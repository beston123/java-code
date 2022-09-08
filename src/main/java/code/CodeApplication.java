package code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 〈启动入口〉<p>
 * 〈功能详细描述〉
 *
 * @author tianwu
 * @date 2022/09/08
 */
@ServletComponentScan
@EnableConfigurationProperties
@SpringBootApplication
public class CodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeApplication.class);
    }

}
