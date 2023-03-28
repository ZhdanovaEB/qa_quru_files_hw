import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;


public class FileReadingFromZipTest {

    private ClassLoader cl = FileParsingTest.class.getClassLoader();

    public String extractZipFile(String archiveName) throws IOException, URISyntaxException {

        URL resource = cl.getResource(archiveName);
        File zipFIle = Paths.get(resource.toURI()).toFile();
        String extractionPath = zipFIle.getParent();
        ZipFile filesZip = new ZipFile(zipFIle);
        filesZip.extractAll(extractionPath);
        return extractionPath;
    }


    @Test
    void zipParseTest() throws Exception {
        final File folder = new File(extractZipFile("testdata/files_hw.zip"));
        for(final File fileEntry : folder.listFiles())
            if (fileEntry.getName().contains(".pdf")) {
                PDF pdf = new PDF(fileEntry);
                Assertions.assertTrue(pdf.text.contains("Тестируем агент нагрузки "));
            }
        else if (fileEntry.getName().contains(".csv")) {
                try (InputStream is = new FileInputStream(fileEntry.getPath());
                     InputStreamReader isr = new InputStreamReader(is)) {
                    CSVReader csvReader = new CSVReader(isr);
                    List<String[]> content = csvReader.readAll();
                    Assertions.assertArrayEquals(new String[]{"Student", " Math", " Geography", " History"}, content.get(0));
                }
            }
        else if (fileEntry.getName().contains(".xlsx")) {
                    XLS xls = new XLS(fileEntry);
                    Assertions.assertTrue(
                            xls.excel.getSheetAt(0).getRow(2).getCell(0).getStringCellValue()
                                    .startsWith("John Smith")
                    );
                }

        }

}
