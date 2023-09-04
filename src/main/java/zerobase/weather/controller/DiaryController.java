package zerobase.weather.controller;

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
    @PostMapping("/create/diary")
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                     LocalDate date, @RequestBody String text) {
        diaryService.createDiary(date, text);

    }

    //일기읽기
    @GetMapping("/read/diary")
    List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        return diaryService.readDiary(date);
    }

    //범위내일기조회
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            LocalDate startDate,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }

    //일기수정
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                     LocalDate date, @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }

    //일기 삭제
    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                     LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
