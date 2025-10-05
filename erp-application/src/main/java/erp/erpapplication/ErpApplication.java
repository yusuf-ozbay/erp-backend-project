package erp.erpapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "erp.commonmodule",
                "erp.crmmodule",
                "erp.invoicemodule"
        }
)
@EnableJpaRepositories(basePackages = {
        "erp.crmmodule.dao",
        "erp.invoicemodule.dao"

})
@EntityScan(basePackages = {
        "erp.crmmodule.models",
        "erp.invoicemodule.models"
})

public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }

}
