package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {
    @Value("${openweathermap.key}")
    private String apiKey;
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    //날씨데이터 자동으로 받아오기
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
        logger.info("get daily weather data");
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    //일기쓰기
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary");
        //DB에서 날씨 데이터 가져오기
        DateWeather dateWeather = getDateWeather(date);

        //파싱 데이터 + 일기 db저장
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setDate(date); //과거 날짜 저장 위해 date 다시 지정
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("end create diary");

    }
    //날씨데이터 자동으로 받아오기
    private DateWeather getWeatherFromApi() {
        //api 데이터 받기
        String weatherData = getWeatherString();
        //json 파싱
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        //entity 저장
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));

        return dateWeather;
    }
    //DB에서 날씨 가져오기
    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.
                findAllByDate(date);
        //DB에 날씨 정보가 없을 때 현재날씨 받아오기
        if (dateWeatherListFromDB.size() == 0 ) {
            return getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }
    //일기읽기
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.info("read diary");
        if(date.isAfter(LocalDate.ofYearDay(3000,1)) |
                date.isBefore(LocalDate.ofYearDay(1900, 1))){
            logger.info("date input error");
            throw new InvalidDate();
        }
        return diaryRepository.findAllByDate(date);
    }

    //기간일기조회
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        logger.info("read Diaries");
        if (startDate.isAfter(LocalDate.ofYearDay(3000,1)) |
                startDate.isBefore(LocalDate.ofYearDay(1900, 1)) |
                endDate.isAfter(LocalDate.ofYearDay(3000,1))|
                endDate.isBefore(LocalDate.ofYearDay(1900,1))){
            logger.info("date input error");
            throw new InvalidDate();
        }
        logger.info("read Diaries complete");
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }
    //일기수정
    public void updateDiary(LocalDate date, String text) {
        logger.info("starting update");
        if (date.isAfter(LocalDate.ofYearDay(3000,1)) |
                date.isBefore(LocalDate.ofYearDay(1900, 1))){
            logger.info("date input error");
            throw new InvalidDate();
        }
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("updated");
    }
    //일기 삭제
    public void deleteDiary(LocalDate date) {
        logger.info("start delete");
        if (date.isAfter(LocalDate.ofYearDay(3000,1)) |
                date.isBefore(LocalDate.ofYearDay(1900, 1))){

            logger.info("date input error");
            throw new InvalidDate();
        }
        diaryRepository.deleteAllByDate(date);
        logger.info("deleted");
    }
    //날씨 api 요청
    private String getWeatherString() {
        logger.info("getting data from api");
        String apiUrl = "https://api.openweathermap.org/data/2.5/" +
                "weather?q=Busan&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }

            br.close();

            logger.info("get api data complete");
            return response.toString();
        } catch (Exception e) {

            logger.error("api error");
            return "failed to get response";

        }
    }
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e) ;
        }
        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        return resultMap;
    }

}
