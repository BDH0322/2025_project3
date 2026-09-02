package com.example.ufc.Controller;

import com.example.ufc.Entity.TotalPredictionEntity;
import com.example.ufc.Entity.VoteBoardEntity;
import com.example.ufc.Repository.TotalPredictionRepository;
import com.example.ufc.Repository.VoteBoardRepository;
import com.example.ufc.Service.CustomUserDetails;
import com.example.ufc.Service.FighterService;
import com.example.ufc.Service.TotalPredictionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.ufc.DTO.VoteBoardDTO;
import com.example.ufc.Service.VoteBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/vote")
@RequiredArgsConstructor
public class VoteBoardController {
   private final VoteBoardService voteBoardService;
    private final FighterService fighterService;
    private final TotalPredictionService totalPredictionService;
    private  final TotalPredictionRepository totalPredictionRepository;//운영자만 작성 페이지 접근 가능
    private final VoteBoardRepository voteBoardRepository;
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/vwrite")
    public String writePage(){
        return "vote/voteWrite";
    }


    /*
    @RequestBody: 클라이언트가 데이터를 JSON(텍스트 문자열)으로 보낼 때 사용한다.
    예: {"title": "제목", "content": "내용"}
        한계: JSON은 텍스트 기반이라 바이너리 데이터인 이미지 파일을 담기에 적합하지 않습니다.

     @ModelAttribute: 클라이언트가 데이터를 Multipart/form-data 형식으로 보낼 때 사용합니다.

    특징: 이름 그대로 데이터를 '여러 부분(Multi-part)'으로 나누어 보냅니다. 한쪽에는 텍스트를,
    다른 한쪽에는 이미지 바이너리를 실어 보낼 수 있습니다.

    FormData 객체는 데이터를 전송할 때 자동으로 Multipart/form-data 형식을 사용합니다.
    Spring에서 이 FormData에 담긴 값들을 추출해서 VoteBoardDTO 객체에 자동으로 바인딩(매핑)해주려면 **@ModelAttribute**가 그 역할을 수행해야 합니다.

     */

    // 투표 게시글 저장(api - 운영자 전용)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/vsave") //js fetch url을 맞춰야함
    @ResponseBody
    public ResponseEntity<?> saveVote(@ModelAttribute VoteBoardDTO dto,
                                      @RequestParam(value = "voteImage",required = false)MultipartFile voteImage){
       //1. 서비스 호출하여 게시글 저장
        voteBoardService.createVote(dto,voteImage);

        //2. 이미지 파일 처리 로직(필요 시 추가)
        if(voteImage != null && !voteImage.isEmpty()){
            System.out.println("업로드된 파이명:" + voteImage.getOriginalFilename());
        }

        return ResponseEntity.ok().build();
    }
    //투표 목록 페이지 뷰
    @GetMapping("/vlist")
    public String voteList(Model mo, @RequestParam(value="page",defaultValue ="0")int page, String keyword, HttpServletRequest request){
        Page<VoteBoardDTO> paging = voteBoardService.getVoteList(page,keyword);
        mo.addAttribute("vote_boardList",paging.getContent());
        mo.addAttribute("paging",paging);
        mo.addAttribute("keyword",keyword);
        // [중요] 헤더에서 사용하는 currentUri 변수를 추가해 줍니다.
        mo.addAttribute("currentUri",request.getRequestURI());
        return "vote/voteList";
    }


    // 정규식 [0-9]+ 를 추가하여 숫자일 때만 이 메서드가 타도록 제한합니다.
    @GetMapping("/{boardNum:[0-9]+}")
    public String voteDetail(@PathVariable Long boardNum, Model mo, HttpServletRequest request, Principal principal){
        VoteBoardDTO dto = voteBoardService.getVoteByBoardNum(boardNum);
        // 2. [추가] FighterService를 통해 실제 이미지 URL 가져오기
        // 필드명이 imageUrl이었으므로, 서비스에서 아까 만든 메서드를 호출합니다.
        String f1Img = fighterService.getImgUrlByName(dto.getFighter1Name());
        String f2Img = fighterService.getImgUrlByName(dto.getFighter2Name());

        // 3. 모델에 이미지 경로 담기
        mo.addAttribute("f1Img", f1Img);
        mo.addAttribute("f2Img", f2Img);

        // 2. [핵심 수정] DB 엔티티를 통해 분석 결과 조회
        // 서비스에서 사용하는 방식과 동일하게 VoteBoardEntity를 먼저 찾은 후 분석 데이터를 조회합니다.
        VoteBoardEntity voteEntity = voteBoardRepository.findByBoardNum(boardNum).orElse(null);
        TotalPredictionEntity tp = null;
        if (voteEntity != null) {
            // 추가하신 레포지토리 메서드를 여기서 사용합니다.
            // [중요] boardNum(6000번 등)이 아니라 연관된 Entity 객체로 찾아야 정확합니다.
            tp = totalPredictionRepository.findByVoteBoard(voteEntity).orElse(null);
        }

        //투표율 계산을 Controller에서 처리하여 뷰에 전달.
        int totalVotes = dto.getFighter1Votes() + dto.getFighter2Votes();
        double f1Rate = totalVotes == 0?0:(double) dto.getFighter1Votes() *100 /totalVotes;
        double f2Rate = totalVotes == 0?0:(double) dto.getFighter2Votes() *100 /totalVotes;

        mo.addAttribute("vote",dto);
        mo.addAttribute("f1Rate",String.format("%.1f",f1Rate));
        mo.addAttribute("f2Rate",String.format("%.1f",f2Rate));
        // 현재 URI를 추가하여 헤더 에러 방지
        mo.addAttribute("currentUri",request.getRequestURI());

        // [중요 추가] DB에서 딥러닝 분석 결과 데이터 가져오기
        // Repository에서 boardNum(ID)으로 찾아서 모델에 담습니다.
        // [핵심 수정] DB에서 딥러닝 분석 결과 가져오기 및 Null 방어
        // 만약 데이터가 없으면 null을 담지 않고 빈 객체를 생성해서 보냅니다.
    /*
    * 타임리프의 **Safe Navigation Operator (?.)**를 사용하면 객체가 null일 때 에러를 내지 않고 조용히 넘어갑니다.
    *totalPrediction?.combinedScore 라고 쓰면, 앞의 값이 null일 때 뒷부분을 실행하지 않으므로 에러가 사라집니다.
    * 상세 페이지를 보여주는 Java 컨트롤러에서 데이터가 없을 때 빈 객체라도 넣어주면 에러가 나지 않습니다.
    *
    * 근데 반전 이것도 오류가 난다 그래서
    * // 1. 데이터가 없는데 빈 객체를 생성(new)하면 필드값이 null이라 타임리프에서 터집니다.
    // 2. findById(boardNum).orElse(null)을 사용하여 데이터가 없으면 확실히 null이 넘어가게 합니다.
    * 1번 방식으로 다시 돌아감
    * */

        // [수정 포인트] 아래 findById 로직은 boardNum(6000번 등)을 PK로 인식해서 null을 뱉습니다.
        // 위에서 findByVoteBoard(voteEntity)로 찾은 tp 값을 그대로 사용하도록 아래 줄은 주석 처리하거나 제거합니다.
        // TotalPredictionEntity tp = totalPredictionRepository.findById(boardNum).orElse(null);//1번 (주석 처리됨)

        // 3. 모델에 담긴 값이 null이면 HTML의 th:if에서 false로 걸러져 에러를 방지합니다.
        mo.addAttribute("totalPrediction",tp);

        //추가: 로그인한 사용자가 이미 투표했는지 여부 확인(서비스 구현)
        boolean hasVoted = false;
        if(principal != null){
            hasVoted = voteBoardService.checkIfVoted(boardNum,principal.getName());
        }
        mo.addAttribute("hasVoted",hasVoted);

        return "vote/voteDetail";
    }


    // 중복 체크 기능 추가
    @PostMapping("/doVote/{boardNum}")
    @ResponseBody
    public ResponseEntity<?> doVote(@PathVariable Long boardNum, @RequestParam int fighterNum, @AuthenticationPrincipal CustomUserDetails customUser){
        if(customUser == null){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        }
        try{
            // [수정 핵심] getUsername() 대신, 클래스에 정의된 getId()를 직접 호출합니다.
            // 이렇게 하면 다른 페이지에 영향을 주지 않고 DB에는 아이디가 저장됩니다.
            String userId = customUser.getId();

            voteBoardService.addVoteWithCheck(boardNum, fighterNum, userId);
            return ResponseEntity.ok().build();

        /*catch (RuntimeException e){
            //이미 투표했을 경우
            return ResponseEntity.badRequest().body(e.getMessage());

            단순히 Exception 하나로 뭉뚱그려 처리하면, 사용자가 투표를 두 번 해서 막힌 건지, 아니면 서버 데이터베이스가 고장 난 건지 알 수 없기 때문에
            중복투표와 서버에러 2가지로 나누었다.

왜 이렇게 구분하는 게 좋은가요?
정확한 안내: 사용자가 "아, 내가 이미 투표했구나"라고 바로 이해하게 하여 불필요한 문의를 줄입니다.

디버깅 효율: 개발자는 로그를 볼 때 400번대 에러는 무시하고, 500번대 에러만 집중적으로 모니터링해서 시스템 결함을 빨리 고칠 수 있습니다.

보안: 서버의 내부 코드 오류 내용을 사용자에게 그대로 보여주면 해킹의 실마리가 될 수 있는데, Exception으로 묶어서 공통 메시지를 내보내면 이를 방지할 수 있습니다.

        }*/
        } catch (IllegalStateException e) {
            // 중복 투표 발생 시 IllegalStateException을 명확히 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 기타 서버 에러 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("투표 처리 중 오류가 발생했습니다.");
        }
    }
    //투표 마감 처리(API - 운영자 전용)
    @PostMapping("/close/{boardNum}") // 주소 형식을 바꿈
    @ResponseBody
    public ResponseEntity<?> closeVote(@PathVariable(name="boardNum") Long boardNum){
        // 1. 투표 마감 (isClosed = 1)
        voteBoardService.closeVote(boardNum);
        // 2. 딥러닝 분석 실행 (파이썬 서버 호출) 같은 위치에서 분석 실행 (컨트롤러를 옮겨 다닐 필요 없음)
        totalPredictionService.finalizePrediction(boardNum);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/voteUpdate/{boardNum}")
    public String updateVote(@PathVariable Long boardNum,
                             @ModelAttribute VoteBoardDTO dto,
                             @RequestParam(value="voteImage", required = false)MultipartFile voteImage){
        voteBoardService.updateVote(boardNum,dto,voteImage);


        //수정이 완료되면 해당 게시글의 상세 페이지로 다시 보냄
        return "redirect:/vote/"+boardNum;
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/voteEdit/{boardNum}")
    public String editPage(@PathVariable Long boardNum,Model mo,HttpServletRequest request){
        VoteBoardDTO dto = voteBoardService.getVoteByBoardNum(boardNum);
        mo.addAttribute("vote",dto);
        mo.addAttribute("currentUri",request.getRequestURI());
        return "vote/voteEdit";
    }
    // 2. 삭제 처리 (GET 또는 POST)
    // <a> 태그로 클릭해서 삭제하려면 GetMapping이 편합니다.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/voteDelete/{boardNum}")

    public String deleteVote(@PathVariable Long boardNum){
        // ================= [수사 결과 반영: 서비스 호출] =================
        // 이 서비스 안에서 totalPredictionRepository.deleteById(boardNum)이
        // 게시글 삭제 전에 먼저 실행되어야 ORA-02292 에러가 나지 않습니다.
        //1.자식 데이터(ai 분석 결과)가 있는지 확인하고 먼저 삭제
       /* if(totalPredictionRepository.existsById(boardNum)){
            totalPredictionRepository.deleteById(boardNum);
        }
        이미지(image_1135ae.png)를 보면 TOTAL_PREDICTION 테이블의 BOARD_NUM 컬럼에 데이터가 남아 있는 이유는 다음과 같습니다.

        1. 왜 삭제가 안 되었을까요? (코드 분석)
        PK 불일치: totalPredictionRepository.existsById(boardNum)는 테이블의 ID(Primary Key) 컬럼에서 해당 번호를 찾습니다. 하지만 이미지 속의 22, 23, 24 같은 번호는 BOARD_NUM이라는 일반 컬럼에 저장된 값입니다.

        조건 불충족: 결과적으로 existsById(boardNum)는 항상 false를 반환하게 되고, 그 안에 있는 deleteById 로직은 아예 실행조차 되지 않은 채 부모 데이터만 삭제된 것입니다.

        2. 해결 방법: 컨트롤러 수정
        컨트롤러에서 직접 삭제하지 말고, 모든 삭제 권한을 **서비스(voteBoardService.deleteVote)**로 넘기세요. 서비스에서 이미 게시글 엔티티를 찾고 있으므로, 그 엔티티를 이용해 자식 데이터를 지우는 것이 가장 확실합니다.


        */


        //2.부모데이터(게시글) 삭제
        voteBoardService.deleteVote(boardNum);
        return "redirect:/vote/vlist";

    }

}
