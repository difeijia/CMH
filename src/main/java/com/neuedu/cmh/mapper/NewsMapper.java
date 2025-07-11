package com.neuedu.cmh.mapper;

import com.neuedu.cmh.entity.News;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NewsMapper {
    @Select("SELECT * FROM news WHERE tenant_id = #{tenantId} " +
            "AND (#{title} IS NULL OR news_title LIKE CONCAT('%', #{title}, '%')) " +
            "AND (#{summary} IS NULL OR news_summary LIKE CONCAT('%', #{summary}, '%')) " +
            "AND (#{author} IS NULL OR author LIKE CONCAT('%', #{author}, '%'))")
    List<News> getNewsList(@Param("title") String title,
                           @Param("summary") String summary,
                           @Param("author") String author,
                           @Param("tenantId") String tenantId);

    @Insert("INSERT INTO news(news_id, news_title, news_image_path, news_content, author, news_summary, tenant_id) " +
            "VALUES(#{news_id}, #{news_title}, #{news_image_path}, #{news_content}, #{author}, #{news_summary}, #{tenant_id})")
    int addNews(News news);

    @Update("UPDATE news SET news_title=#{news_title}, news_image_path=#{news_image_path}, " +
            "news_content=#{news_content}, author=#{author}, news_summary=#{news_summary} " +
            "WHERE news_id=#{news_id}")
    int updateNews(News news);

    @Delete("DELETE FROM news WHERE news_id=#{newsId}")
    int deleteNews(String newsId);

    @Select("SELECT * FROM news WHERE news_id=#{newsId}")
    News getNewsById(String newsId);

    @Select("SELECT * FROM news " +
            "WHERE (#{title} IS NULL OR news_title LIKE CONCAT('%', #{title}, '%')) " +
            "AND (#{summary} IS NULL OR news_summary LIKE CONCAT('%', #{summary}, '%')) " +
            "AND (#{author} IS NULL OR author LIKE CONCAT('%', #{author}, '%')) " +
            "LIMIT #{size} OFFSET #{offset}")
    List<News> getAllNewsList(@Param("title") String title,
                              @Param("summary") String summary,
                              @Param("author") String author,
                              @Param("offset") int offset,
                              @Param("size") int size);

    @Select("SELECT COUNT(*) FROM news " +
            "WHERE (#{title} IS NULL OR news_title LIKE CONCAT('%', #{title}, '%')) " +
            "AND (#{summary} IS NULL OR news_summary LIKE CONCAT('%', #{summary}, '%')) " +
            "AND (#{author} IS NULL OR author LIKE CONCAT('%', #{author}, '%'))")
    int countNews(@Param("title") String title,
                  @Param("summary") String summary,
                  @Param("author") String author);
}