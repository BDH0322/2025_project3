package com.example.ufc.Repository;

import com.example.ufc.Entity.FighterEntity;
import com.example.ufc.Entity.IntegratedSearchLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IntegratedSearchLogRepository extends JpaRepository<IntegratedSearchLogEntity, Long> {

    @Query(value =
            "SELECT * FROM ( " +
                    "    SELECT f.* " +
                    "    FROM integrated_search_log l " +
                    "    JOIN fighter f ON l.fighter_entity_name = f.name " +
                    "    WHERE l.search_time >= :since " +
                    "    GROUP BY f.name, f.height, f.weight, f.reach, f.stance, f.dob, f.slpm, " +
                    "             f.stracc, f.sapm, f.strdef, f.tdavg, f.tdacc, f.tddef, f.subavg, " +
                    "             f.weight_class, f.rank_num, f.weight_code, f.total, f.wins, " +
                    "             f.ko_tko, f.sub_wins, f.dec_wins, f.losses, f.draws, f.avg_time, " +
                    "             f.fighter_code, f.image_url " +
                    "    ORDER BY COUNT(l.log) DESC " +
                    ") WHERE ROWNUM <= 5", nativeQuery = true)
    List<FighterEntity> findPopularFighters(@Param("since") LocalDateTime since);

    @Query(
            value = """
        SELECT *
        FROM INTEGRATED_SEARCH_LOG
        WHERE SEARCH_TIME > :time
    """,
            nativeQuery = true
    )
    List<IntegratedSearchLogEntity> SearchTime(
            @Param("time") LocalDateTime time
    );
}
