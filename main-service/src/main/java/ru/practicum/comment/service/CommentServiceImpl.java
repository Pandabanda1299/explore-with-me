package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDtoRequest;
import ru.practicum.comment.dto.CommentDtoResponse;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.specification.DbCommentSpecification;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentServiceImpl implements CommentService {

    final CommentRepository commentRepository;
    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDtoResponse create(Long userId, Long eventId, CommentDtoRequest dto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие " + eventId + " не найдено"));

        if (event.getState() != State.PUBLISHED) {
            throw new ValidationException("Событие " + eventId + " не опубликовано");
        }

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь " + userId + " не найден"));

        Comment comment = commentRepository.save(commentMapper.toEntity(dto, event, user, LocalDateTime.now()));
        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public CommentDtoResponse update(Long userId, Long eventId, Long commId, CommentDtoRequest dto) {
        Comment comment = getCommentCheckParams(userId, eventId, commId);
        comment.setText(dto.getText());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long eventId, Long commId) {
        getCommentCheckParams(userId, eventId, commId);
        commentRepository.deleteById(commId);
    }

    @Override
    public CommentDtoResponse findCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий " + commentId + " не найден"));
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDtoResponse> findCommentsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие " + eventId + " не найдено");
        }

        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return commentMapper.toDto(comments);
    }

    @Override
    public List<CommentDtoResponse> findCommentsByUserIdAndEventId(Long userId, Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие " + eventId + " не найдено");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }

        List<Comment> comments = commentRepository.findAllByUserIdAndEventId(userId, eventId);
        return commentMapper.toDto(comments);
    }

    @Override
    public List<CommentDtoResponse> findCommentsAdmin(List<Integer> users, List<Integer> events,
                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                      Integer from, Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания ");
        }
        Specification<Comment> spec = DbCommentSpecification.getSpecificationAdmin(users, events, rangeStart, rangeEnd);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAll(spec, pageable).getContent();

        return commentMapper.toDto(comments);
    }

    @Transactional
    @Override
    public void deleteCommentAdmin(Long commId) {
        if (!commentRepository.existsById(commId)) {
            throw new NotFoundException("Комментарий " + commId + " не существует");
        }
        commentRepository.deleteById(commId);
    }

    private Comment getCommentCheckParams(Long userId, Long eventId, Long commId) {
        return commentRepository.findCommentWithUserAndEvent(commId, eventId, userId)
                .orElseThrow(() -> {
                    if (!commentRepository.existsById(commId)) {
                        return new NotFoundException("Комментарий " + commId + " не найден");
                    }
                    if (!eventRepository.existsById(eventId)) {
                        return new NotFoundException("Событие " + eventId + " не найдено");
                    }
                    if (!userRepository.existsById(userId)) {
                        return new NotFoundException("Пользователь " + userId + " не найден");
                    }
                    return new ConflictException("Комментарий не принадлежит пользователю " + userId + " или событию "
                            + eventId);
                });
    }
}
