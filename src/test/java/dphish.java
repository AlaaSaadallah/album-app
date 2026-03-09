import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public class dphish {
    ChromeDriver driver;
    SoftAssert softAssert;

    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup(); // مهم عشان يجيبلك الدرايفر المناسب
        softAssert = new SoftAssert();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://testing.dphish.live/login");
    }

    @Test(priority = 1)
    public void login() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        try {
            WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            username.sendKeys("ahmed.wagdy@dphish.com");
            System.out.println("✅ Username field found and value entered.");
        } catch (TimeoutException e) {
            System.out.println("❌ Username field not found. Check locator!");
        }

        try {
            WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            password.sendKeys("Ahmed@1234");
            System.out.println("✅ Password field found and value entered.");
        } catch (TimeoutException e) {
            System.out.println("❌ Password field not found. Check locator!");
        }

        try {
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
            loginBtn.click();
            System.out.println("✅ Login button clicked.");
        } catch (TimeoutException e) {
            System.out.println("❌ Login button not found. Check locator!");
        }

        // ✅ بدل Thread.sleep() بخط الانتظار دا
        wait.until(ExpectedConditions.urlToBe("https://testing.dphish.live/admin-panel"));
        String actualURL = driver.getCurrentUrl();
        String expectedURL = "https://testing.dphish.live/admin-panel";
        Assert.assertEquals(actualURL, expectedURL, "❌ URL is not as expected after login");
    }

    @Test(priority = 2)
    public void create_campaign() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        try {
            WebElement Do = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".px-7.flex.flex-col.gap-8.overflow-y-hidden.overflow-x-hidden.items-center")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", Do);
            System.out.println("✅ Scrolled to 'Do' element instead of hover.");
            Actions actions = new Actions(driver);
            actions.moveToElement(Do).build().perform();
            System.out.println("✅ Moved to 'Do' element.");
        } catch (TimeoutException e) {
            System.out.println("❌ 'Do' element not found. Check locator or text!");
        }

        try {
            WebElement svgIcon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[name()='svg' and @viewBox='0 0 512 512']")));

            // 🔍 تحقق من حالة القائمة قبل الضغط
            WebElement doButton = driver.findElement(By.cssSelector("div[id='hs-basic-heading-Do_1'] button"));
            String beforeClick = doButton.getAttribute("aria-expanded");
            System.out.println("📁 Dropdown status BEFORE click: " + beforeClick);

            svgIcon.click();
            System.out.println("✅ Clicked on SVG icon successfully.");

            // انتظر شوية بسيطة بعد الكليك
            Thread.sleep(700);

            // 🔍 تحقق من حالتها بعد الضغط
            String afterClick = doButton.getAttribute("aria-expanded");
            System.out.println("📂 Dropdown status AFTER click: " + afterClick);

            // من هنا تقدر تعمل شرط لو لسه مقفولة
            if (afterClick == null || afterClick.equals("false")) {
                System.out.println("⚠️ Dropdown still closed, clicking again...");
                svgIcon.click();
            }

        } catch (TimeoutException e) {
            System.out.println("❌ SVG element not found or not clickable.");
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    public void CHOOSE_CAMPAIGN_TYPE() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        WebElement doButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div[id='hs-basic-heading-Do_1'] button")));
        doButton.click();

        WebElement doButtonAgain = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div[id='hs-basic-heading-Do_1'] button")));
        doButtonAgain.click();

        WebElement smartCampaignLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/admin-panel/do/smart-campaign']")));
        smartCampaignLink.click();

        // ✅ هنا بدل الـ sleep بانتظار الـ URL
        wait.until(ExpectedConditions.urlContains("/admin-panel/do/smart-campaign"));

        wait.until(ExpectedConditions.urlContains("/admin-panel/do/smart-campaign"));
        String actualURL = driver.getCurrentUrl();
        Assert.assertTrue(actualURL.contains("/admin-panel/do/smart-campaign"),
                "❌ URL is not as expected after navigating to smart campaign. Actual: " + actualURL);
    }

    @Test(priority = 4)
    public void clickNewCampaignButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        try {
            // استنى الزرار لحد ما يبقى clickable
            // WebElement newCampaignBtn = wait.until(ExpectedConditions.elementToBeClickable(
            //          By.xpath("//button[contains(.,'New Campaign')]")
            //));
            WebElement newCampaignBtn = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(.,'New Campaign')]"))
            );
            newCampaignBtn.click();
            System.out.println("✅ Clicked on 'New Campaign' button successfully.");
        } catch (TimeoutException e) {
            System.out.println("❌ 'New Campaign' button not found or not clickable.");
        }

        wait.until(ExpectedConditions.urlContains("/add-campaign"));

        String actualURL = driver.getCurrentUrl();
        String expectedURL = "https://testing.dphish.live/admin-panel/do/smart-campaign/add-campaign";
        Assert.assertEquals(actualURL, expectedURL, "❌ Add Campaign URL is wrong!");
    }

    @Test(priority = 5)
    public void fillCampaignDetails() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        WebElement campaignName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        campaignName.clear();
        campaignName.sendKeys("wigs700");


// 1- اضغط على الـ dropdown عشان يفتح
        // 1- افتح الـ dropdown
        // 1- افتح الـ dropdown
        WebElement successCategoryDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("div[name='successCategory'] .vs__dropdown-toggle"))
        );
        successCategoryDropdown.click();

// 2- استنى الليست تظهر
        WebElement dropdownList = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul[id*='listbox']"))
        );

// 3- اختار الـ option المطلوب
        WebElement credentialSavedOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(.,'Credential Saved')]"))
        );
        credentialSavedOption.click();

        System.out.println("✅ 'Credential Saved' selected successfully.");
// 1️⃣ افتح الـ Attack dropdown
        WebElement attackDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("div[name='attack'] .vs__dropdown-toggle"))
        );
        attackDropdown.click();
        System.out.println("✅ Attack dropdown opened.");

// 2️⃣ استنى الليست تظهر
        WebElement attackDropdownList = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul[id*='listbox']"))
        );
        WebElement scrollContent = driver.findElement(By.cssSelector("#main-scrollbar .scroll-content"));

        // Scroll لـ 500px
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.transform = 'translate3d(0px, -450px, 0px)'",
                scrollContent
        );
// 3️⃣ اختار الـ option "No Attack"
        By noAttackOption = By.xpath("//li[normalize-space()='No Attack']");

        wait.until(ExpectedConditions.elementToBeClickable(noAttackOption)).click();
        System.out.println("✅ 'No Attack' option selected successfully.");


        /*

        // 1️⃣ افتح الدروب داون "Attack"
        WebElement attackDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("div[name='attack'] .vs__dropdown-toggle"))
        );
        attackDropdown.click();
        System.out.println("✅ Attack dropdown opened.");
        WebElement scrollContent = driver.findElement(By.cssSelector("#main-scrollbar .scroll-content"));

        // Scroll لـ 500px
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.transform = 'translate3d(0px, -350px, 0px)'",
                scrollContent
        );
*/
// ✅ 1️⃣ افتح dropdown "Difficulty Level" بالاعتماد على الـ placeholder
        WebElement difficultyDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@placeholder='Choose Difficulty Level']/ancestor::div[contains(@class,'vs__dropdown-toggle')]")
                )
        );

// Scroll قبل الضغط علشان يتأكد إنه ظاهر
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", difficultyDropdown);
        difficultyDropdown.click();
        System.out.println("✅ Difficulty Level dropdown opened.");

// ✅ 2️⃣ استنى الـ options تظهر
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(.,'Easy')]")));

// ✅ 3️⃣ دوس على Easy
        WebElement easyOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(.,'Easy')]")));
        easyOption.click();
        System.out.println("✅ 'Easy' selected successfully.");
        // افتح dropdown "Tracker Host"
        WebElement trackerDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@id='vs4__combobox']//input[@type='search']/ancestor::div[contains(@class,'vs__dropdown-toggle')]")
                )
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", trackerDropdown);
        trackerDropdown.click();
        System.out.println("✅ Tracker Host dropdown opened.");

// حدد الـ search input داخل الـ dropdown
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[@id='vs4__combobox']//input[@type='search']")
                )
        );

// اكتب الاسم في البحث
        String targetOption = "testing.allnes.xyz";
        searchInput.sendKeys(targetOption);

// استنى العنصر يظهر واختاره فعليًا
        WebElement optionToSelect = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//li[contains(@id,'vs4__option') and normalize-space()='" + targetOption + "']")
                )
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", optionToSelect);
        System.out.println("✅ '" + targetOption + "' selected successfully from dropdown.");
        //Thread.sleep(6000);

        By nextButtonLocator = By.xpath("//button[normalize-space()='Next']");

        try {
            WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(nextButtonLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
            nextButton.click();
            System.out.println("✅ Next button clicked successfully (normal click).");
        } catch (TimeoutException e) {
            System.out.println("⚠️ Next button not clickable, trying JS click...");
            WebElement nextButton = driver.findElement(nextButtonLocator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);
            System.out.println("✅ Next button clicked using JS.");

        }


    }

    @Test(priority = 6)
    public void choose_campaign_type() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        // اختر العنصر وانتظر يكون قابل للنقر
        WebElement emailWithPageBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//label[@for='type-EWCT']")
                )
        );

        // Scroll للعنصر علشان يكون ظاهر على الشاشة
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", emailWithPageBtn);

        // اضغط على الزرار باستخدام JavaScript
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", emailWithPageBtn);

        System.out.println("✅ 'Email With Page' selected successfully.");

        // تحقق إن الراديو اتعمل عليه
        WebElement radioInput = driver.findElement(By.id("type-EWCT"));
        if (radioInput.isSelected()) {
            System.out.println("☑️ Radio 'Email With Page' is selected ✅");
        } else {
            System.out.println("⚠️ Radio 'Email With Page' is NOT selected ❌");

        }
        By nextButtonLocator = By.xpath("//button[normalize-space()='Next']");

        try {
            WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(nextButtonLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
            nextButton.click();
            System.out.println("✅ Next button clicked successfully (normal click).");
       } catch (TimeoutException e) {
            System.out.println("⚠️ Next button not clickable, trying JS click...");
            WebElement nextButton = driver.findElement(nextButtonLocator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);
            System.out.println("✅ Next button clicked using JS.");

        }



        Thread.sleep(6000);
    }


@Test(priority = 7)
public void choose_sender()throws  InterruptedException
{
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    By searchInputLocator = By.xpath("//input[@placeholder='Search']");

    WebElement searchInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(searchInputLocator)
    );

// click + focus
    ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});" +
                    "arguments[0].click();" +
                    "arguments[0].focus();",
            searchInput
    );

// clear properly
    searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
    searchInput.sendKeys(Keys.DELETE);

// type like real user
    searchInput.sendKeys("supportt@fammy-ce.it.com");

// force React to listen
    ((JavascriptExecutor) driver).executeScript(
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            searchInput
    );

    System.out.println("✅ Search input filled and React notified.");
    Thread.sleep(6000);
    By nextButtonLocator = By.xpath("//button[normalize-space()='Next']");

    try {
        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(nextButtonLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
        nextButton.click();
        System.out.println("✅ Next button clicked successfully (normal click).");
    } catch (TimeoutException e) {
        System.out.println("⚠️ Next button not clickable, trying JS click...");
        WebElement nextButton = driver.findElement(nextButtonLocator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);
        System.out.println("✅ Next button clicked using JS.");
        Thread.sleep(6000);

    }
}


    // }
    @AfterTest
    public void CLOSE() {
        driver.quit();
    }
}

