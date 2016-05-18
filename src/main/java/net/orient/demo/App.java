package net.orient.demo;

import com.google.common.collect.Lists;
import net.orient.demo.config.RootConfiguration;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import net.orient.demo.graph.dao.IdentityGraph;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private static boolean webApplicationContextInitialized = false;
    public static void main( String[] args ) throws IOException {
        FrontEndCLI fcli = new FrontEndCLI();
        Args.parse(fcli,args);
        if(fcli.help){
            Args.usage(fcli);
            System.exit(0);
        }
        if(fcli.propertiesFileLocation == null){
            Args.usage(fcli);
            throw new IOException("Properties file not specified.");
        }
        System.getProperties().setProperty("location",fcli.propertiesFileLocation);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

            /*
             * One problem with SpringMVC is it creates its own application
             * context, and so it can end up failing but our application will
             * keep running.
             *
             * To detect the case where the SpringMVC's web application context
             * fails we'll listen for ContextRefreshEvents and set a flag when
             * we see one.
             */
        context
                .addApplicationListener(new ApplicationListener<ContextRefreshedEvent>() {
                    public void onApplicationEvent(
                            ContextRefreshedEvent event) {
                        ApplicationContext ctx = event.getApplicationContext();
                        if (ctx instanceof AnnotationConfigWebApplicationContext) {
                            webApplicationContextInitialized = true;
                        }
                    }
                });
        context.registerShutdownHook();
        context.register(RootConfiguration.class);
        context.refresh();
        if (!webApplicationContextInitialized) {
            System.out.println("Web application context not initialized. Exiting.");
            System.exit(1);
        }
        System.out.println(String.format("\nJetty Application started, available at " + "%s\n", getURI()));
        if(fcli.links != null){
            loadGraph(fcli, context);
        }
        while(true){
            // no op
        }
    }

    private static void loadGraph(FrontEndCLI fcli, AnnotationConfigApplicationContext context) throws IOException {
        IdentityGraph graph = (IdentityGraph)context.getBean("identityGraph");
        BufferedReader reader = new BufferedReader(new FileReader(fcli.links));
        String line = reader.readLine();
        while(line != null){
            List<String> links = Lists.newArrayList(line.split(",")).stream().filter(e -> !e.equals("")).collect(Collectors.toList());
            if(links.size() > 1){
                for(int i = 0; i < links.size(); i++){
                    if(i < links.size() - 1){
                        graph.addLink(links.get(i), links.get(i+1));
                    }
                }
            } else if(links.size() == 1){
                graph.addSingleLocal(links.get(0));
            }
            line = reader.readLine();
        }
    }

    private static URI getURI() {
        return UriBuilder.fromUri("http://" + getHostName() + "/").port(8085).build();
    }
    private static String getHostName() {
        String hostName = "localhost";
        try{
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
        return hostName;
    }
    public static class FrontEndCLI{
        @Argument(value = "properties", alias = "p", description = "Properties file location with at least the following " +
                "defined: \n    orientdbURL", required = true)
        private String propertiesFileLocation;
        @Argument(value = "links", alias = "l", description = "links csv file for loading the graph database", required = false)
        private String links;
        @Argument(value = "help", alias = "h", description = "This help Menu", required = false)
        private boolean help;
    }
}
