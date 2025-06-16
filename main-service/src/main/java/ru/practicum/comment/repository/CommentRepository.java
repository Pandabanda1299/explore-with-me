package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    List<Comment> findAllByEventId(Long eventId);

    List<Comment> findAllByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user u " +
            "JOIN FETCH c.event e " +
            "WHERE c.id = :commentId AND e.id = :eventId AND u.id = :userId")
    Optional<Comment> findCommentWithUserAndEvent(
            @Param("commentId") Long commentId,
            @Param("eventId") Long eventId,
            @Param("userId") Long userId
    );


}
