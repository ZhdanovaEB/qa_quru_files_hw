import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;

public class FileParsingTest {

        private ClassLoader cl = FileParsingTest.class.getClassLoader();

        @Test
        void pdfParseTest() throws Exception {
            Selenide.open("https://junit.org/junit5/docs/current/user-guide/");
            File download = $("a[href*='junit-user-guide-5.9.2.pdf']").download();
            PDF pdf = new PDF(download);
            Assertions.assertEquals(
                    "Stefan Bechtold, Sam Brannen, Johannes Link, Matthias Merdes, Marc Philipp, Juliette de Rancourt, Christian Stein",
                    pdf.author
            );
        }

        @Test
        void xlsParseTest() throws Exception {
            Selenide.open("https://excelvba.ru/programmes/Teachers?ysclid=lfcu77j9j9951587711");
            File download = $("a[href*='teachers.xls']").download();
            XLS xls = new XLS(download);

            Assertions.assertTrue(
                    xls.excel.getSheetAt(0).getRow(3).getCell(2).getStringCellValue()
                            .startsWith("1. Суммарное количество часов планируемое на штатную по всем разделам плана")
            );
        }

        @Test
        void csvParseTest() throws Exception {
            try (InputStream is = cl.getResourceAsStream("qaguru.csv");
                 InputStreamReader isr = new InputStreamReader(is)) {
                CSVReader csvReader = new CSVReader(isr);
                List<String[]> content = csvReader.readAll();
                Assertions.assertArrayEquals(new String[] {"Тучс", "JUnit5"}, content.get(1));
            }
        }

        @Test
        void filesEqulsTest() throws Exception {
            Selenide.open("https://github.com/qa-guru/qa_guru_18_files/blob/master/src/test/resources/qaguru.csv");
            File download = $("#raw-url").download();
            try (InputStream isExpected = cl.getResourceAsStream("expectedfiles/qaguru.csv");
                 InputStream downloaded = new FileInputStream(download)) {
                Assertions.assertEquals(
                        Objects.hash(isExpected.readAllBytes()),
                        Objects.hash( downloaded.readAllBytes())
                );
            }
        }

        @Test
        void zipTest() throws Exception {
            try (InputStream is = cl.getResourceAsStream("sample.zip");
                 ZipInputStream zs = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zs.getNextEntry()) != null) {
                    Assertions.assertTrue(entry.getName().contains("sample.txt"));
                }
            }
        }

        @Test
        void jsonTest() throws Exception {
            Gson gson = new Gson();
            try (InputStream is = cl.getResourceAsStream("human.json");
                 InputStreamReader isr = new InputStreamReader(is)) {
                JsonObject jsonObject = gson.fromJson(isr, JsonObject.class);

                Assertions.assertTrue(jsonObject.get("isClever").getAsBoolean());
                Assertions.assertEquals(33, jsonObject.get("age").getAsInt());
            }
        }

        @Test
        void jsonCleverTest() throws Exception {
            Gson gson = new Gson();
            try (InputStream is = cl.getResourceAsStream("human.json");
                 InputStreamReader isr = new InputStreamReader(is)) {
                Human human = gson.fromJson(isr, Human.class);

                Assertions.assertTrue(human.isClever);
                Assertions.assertEquals(33, human.age);
            }
        }

}
