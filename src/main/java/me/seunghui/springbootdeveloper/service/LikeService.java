package me.seunghui.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.seunghui.springbootdeveloper.Repository.ArticleRepository;
import me.seunghui.springbootdeveloper.Repository.LikeRepository;
import me.seunghui.springbootdeveloper.Repository.UserRepository;
import me.seunghui.springbootdeveloper.domain.Article;
import me.seunghui.springbootdeveloper.domain.Like;
import me.seunghui.springbootdeveloper.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class LikeService {
    private final LikeRepository likeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
// public boolean addLike(Long articleId, Long userId,String userName)
    //게시글에 맞는 좋아요 생성
    @Transactional
    public boolean addLike(Long articleId,String userName) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        log.info("ArticleId:{} " , articleId);
        Optional<Like> existingLike = likeRepository.findByArticleAndUser(articleId, user.getId());

        if(user.getEmail()==userName){
            log.info("email: {} already exists", user.getEmail());
            log.info("userName: {} already exists", userName);
        }else{
            log.info("email: {} ", user.getEmail());
            log.info("userName: {}", userName);
        }


        if (existingLike.isPresent()) {
            Like updateLike=existingLike.get();
            // 현재 상태를 반대로 변경
            updateLike.changeLikedStatus(updateLike.isLikedStatus());

            likeRepository.save(updateLike); // 변경 사항 저장
            return updateLike.isLikedStatus();
        } else {
            Like newLike=Like.builder()
                    .article(article)
                    .user(user)
                    .likedStatus(true)
                    .build();
            likeRepository.save(newLike);  // 좋아요 추가
             // 좋아요 상태가 true로 변경됨
            return newLike.isLikedStatus();
        }
    }
    //좋아요뷰단에서 취소하면 delete 되는게 아니라 likedStatus가 0(false)으로 됨
    //한 게시글에 대한 좋아요 누른 사용자 목록 조회
    @Transactional(readOnly = true)
    public boolean checkLikeStatus(Long articleId, String userName) {
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Optional<Like> like = likeRepository.findByArticleAndUser(articleId, user.getId());

        // 좋아요가 존재하고 likedStatus가 true이면 true 반환
        return like.isPresent() && like.get().isLikedStatus();
    }

    @Transactional
    public long getLikeCount(long articleId) {
        return likeRepository.countLikesByArticleId(articleId);
    }

}