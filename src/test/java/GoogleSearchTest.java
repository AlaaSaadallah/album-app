import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GoogleSearchTest {
    ChromeDriver driver;

    @BeforeClass
    public void setUp() {
        // Path للـ chromedriver لو مش حاططها في PATH

        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void openGoogleAndFindElementById() throws InterruptedException {
        driver.get("https://www.google.com");

        // الوصول لعنصر البحث باستخدام الـ ID
       driver.findElement(By.id("APjFqb")).sendKeys("Selenium");
       Thread.sleep(3000);
    }

    @AfterClass
    public void tearDown() {
            driver.quit();
        }
    }


