package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {
    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    //일기쓰기
    @ApiOperation("일기 텍스트와 날씨 DB에 저장")
    @PostMapping("/create/diary")
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                     @ApiParam(value = "날짜 형식 : yyyy-MM-dd", example = "2023-09-04")
                     LocalDate date, @RequestBody String text) {
        diaryService.createDiary(date, text);

    }

    //일기읽기
    @ApiOperation("특정 날짜 모든 일기 가져오기")
    @GetMapping("/read/diary")
    List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                          @ApiParam(value = "날짜 형식 : yyyy-MM-dd", example = "2023-09-04")
                          LocalDate date) {
        return diaryService.readDiary(date);
    }

    //범위내일기조회
    @ApiOperation("기간 내 모든 일기 가져오기")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회 기간 첫번째날", example = "2023-08-01")
                            LocalDate startDate,
                            @RequestParam
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회 기간 마지막날", example = "2023-09-04")
                            LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }

    //일기수정
    @ApiOperation("특정 날짜 일기 수정")
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                     @ApiParam(value = "날짜 형식 : yyyy-MM-dd", example = "2023-09-04")
                     LocalDate date, @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }

    //일기 삭제
    @ApiOperation(value = "특정 날짜 일기 삭제", notes = "날짜와 일치하는 일기 모두 삭제")
    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                     @ApiParam(value = "날짜 형식 : yyyy-MM-dd", example = "2023-09-04")
                     LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
