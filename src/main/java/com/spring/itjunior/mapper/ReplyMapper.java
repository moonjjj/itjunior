package com.spring.itjunior.mapper;

import com.spring.itjunior.domain.Reply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReplyMapper {

    //댓글 모두 가져오기
    public List<Reply> selectAll(int free_idx);

    //부모 댓글 추가
    public int insertParent(Reply reply);

    //대댓글 추가
    public int insertChild(Reply reply);

    //댓글 갯수
    public int replycnt(int free_idx);
}