package org.jboss.windup.testutil.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Used to assist the unit tests in validating the contents of the EJB Report.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestEJBReportUtil extends TestChromeDriverReportUtil {
    public enum EJBType {
        MDB,
        STATELESS,
        STATEFUL,
        ENTITY
    }

    /**
     * Checks that an EJB of the given type and classname is listed with given columns in the table
     */
    public boolean checkBeanInReport(EJBType ejbType, String... columns) {
        String tableID;
        switch (ejbType) {
            case MDB:
                tableID = "mdbTable";
                break;
            case STATELESS:
                tableID = "statelessTable";
                break;
            case STATEFUL:
                tableID = "statefulTable";
                break;
            case ENTITY:
                tableID = "entityTable";
                break;
            default:
                throw new IllegalArgumentException("Unexpected type: " + ejbType);
        }

        WebElement element = getDriver().findElement(By.id(tableID));
        if (element == null) {
            throw new CheckFailedException("Unable to find ejb beans table element");
        }
        return checkValueInTable(element, columns);
    }
}
