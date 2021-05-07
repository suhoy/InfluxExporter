package exporter.influx;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.influxdb.dto.QueryResult;
import static exporter.influx.Main.debug;

public class Utils {

    public static void debugMessage(String strOut) {
        if (debug) {
            System.out.println(strOut);
        }
    }

    //московское время в utc (для influx)
    public static String convertToUTC(String dateMoscow) {
        DateTimeFormatter DATE_TIME_FORMATTER_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        DateTimeFormatter DATE_TIME_FORMATTER_OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        dateMoscow = dateMoscow + "+03:00";
        OffsetDateTime dateOff = OffsetDateTime.parse(dateMoscow, DATE_TIME_FORMATTER_INPUT);
        OffsetDateTime dateUTC = dateOff.withOffsetSameInstant(ZoneOffset.UTC);
        String outUTC = dateUTC.format(DATE_TIME_FORMATTER_OUTPUT) + "Z";
        return outUTC;
    }

    //sout листа массива
    public static void writeItOut(List<String[]> strOut) {
        System.out.println("\r\n");
        for (String[] strings : strOut) {
            System.out.println(String.join(";", strings));
        }
    }

    //pretty print in string  List<String[]>
    public static String getLine(List<String[]> strOut) {
        StringBuilder sb = new StringBuilder();
        for (String[] strings : strOut) {
            sb.append(String.join(";", strings));
            sb.append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    //имя папки по текущему времени
    public static String getFolder() {
        Date date = new Date();
        SimpleDateFormat form = new SimpleDateFormat("yyyy_MM_dd'T'HH_mm_ss");
        return form.format(date);
    }

    //deprecated 
    public static double getTimeCoef(String startTime, String finishTime) {
        DateTimeFormatter DATE_TIME_FORMATTER_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        startTime = startTime + "+03:00";
        finishTime = finishTime + "+03:00";
        OffsetDateTime dateStart = OffsetDateTime.parse(startTime, DATE_TIME_FORMATTER_INPUT);
        OffsetDateTime dateFinish = OffsetDateTime.parse(finishTime, DATE_TIME_FORMATTER_INPUT);
        return Duration.between(dateStart, dateFinish).toMinutes() / 60.0;
    }

    //считаем период на основании duration
    public static String sumTime(String startTime, String duration) throws Exception {
        DateTimeFormatter DATE_TIME_FORMATTER_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        DateTimeFormatter DATE_TIME_FORMATTER_OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        startTime = startTime + "+03:00";
        String[] dur = duration.split(":");

        OffsetDateTime start = OffsetDateTime.parse(startTime, DATE_TIME_FORMATTER_INPUT);

        start = start.plusHours(Long.parseLong(dur[0]));
        start = start.plusMinutes(Long.parseLong(dur[1]));
        start = start.plusSeconds(Long.parseLong(dur[2]));

        String finish = start.format(DATE_TIME_FORMATTER_OUTPUT);
        return finish;
    }

    //парсилка ответа инфлюкса
    public static String[][] Parse(QueryResult qr, String start, String finish, String duration, String profile) {
        //парсим заголовки таблицы
        String[] tags = qr.getResults().get(0).getSeries().get(0).getTags().keySet().toString().replace("[", "").replace("]", "").split(", ");
        String[] columns = qr.getResults().get(0).getSeries().get(0).getColumns().toString().replace("[", "").replace("]", "").split(", ");
        String[] header = ArrayUtils.addAll(tags, columns);

        //считаем размеры таблицы (2 строки под время, 2 строки под profile и duration,  1 под названия столбцов)
        int offSetRows = 2 + 2 + 1;
        int countColumns = header.length;
        int countRows = qr.getResults().get(0).getSeries().size() + offSetRows;

        String[][] result = new String[countRows][countColumns];

        result[0] = new String[countColumns];
        result[1] = new String[countColumns];
        result[0][0] = "start";
        result[0][1] = "finish";
        result[1][0] = start;
        result[1][1] = finish;

        result[2] = new String[countColumns];
        result[3] = new String[countColumns];
        result[2][0] = "duration";
        result[2][1] = "profile";
        result[3][0] = duration;
        result[3][1] = profile;

        // теги и название столбцов
        result[4] = header;

        //заполнение значениями
        for (int i = offSetRows; i < countRows; i++) {
            result[i] = ArrayUtils.addAll(qr.getResults().get(0).getSeries().get(i - offSetRows).getTags().values().toString().replace("[", "").replace("]", "").split(", "), qr.getResults().get(0).getSeries().get(i - offSetRows).getValues().toString().replace("[", "").replace("]", "").split(", "));
        }

        //вывод в лог
        for (String[] arr : result) {
            Utils.debugMessage(Arrays.toString(arr));
        }

        return result;
    }

    //убираем лишние символы для экселя
    public static String InfluxTimeToXlsxTime(String time) {
        return time.replace("T", " ").replace("Z", "");
    }

    public static LocalDateTime StringToLocalDateTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        return dateTime;
    }
}
