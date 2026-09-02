package com.example.ufc.Repository;

import com.example.ufc.Entity.MemberEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity,String> {
    @Query(value = "SELECT * FROM ( " +
            "  SELECT a.*, ROWNUM AS rnum FROM ( " +
            "    SELECT * FROM UFCGGMEMBER " +
            "    WHERE ID LIKE '%' || :keyword1 || '%' " +
            "       OR NAME LIKE '%' || :keyword2 || '%' " +
            "    ORDER BY ID ASC " +
            "  ) a WHERE ROWNUM <= :#{#pageable.offset + #pageable.pageSize} " +
            ") WHERE rnum > :#{#pageable.offset} --",
            countQuery = "SELECT count(*) FROM UFCGGMEMBER WHERE ID LIKE '%' || :keyword1 || '%' OR NAME LIKE '%' || :keyword2 || '%'",
            nativeQuery = true)
    Page<MemberEntity> findUserId(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2, Pageable pageable);
}
