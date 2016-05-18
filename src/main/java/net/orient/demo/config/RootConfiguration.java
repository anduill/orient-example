package net.orient.demo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;

/**
 * The root application context.
 *
 * Beans can also be configured by XML in root-context.xml which is imported by
 * this context class.
 *
 * Component scanning is also done to pickup any components other than
 * @Controllers. @Controllers will be picked up by the SpringMVC context.
 */
@Configuration
@ImportResource("classpath:META-INF/spring/Beans.xml")
@Import({ net.orient.demo.config.JettyConfiguration.class })
@ComponentScan(basePackages = { "net.orient.demo.web" }, excludeFilters = { @ComponentScan.Filter(Controller.class) })
public class RootConfiguration {
}
