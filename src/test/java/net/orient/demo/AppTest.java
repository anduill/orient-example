package net.orient.demo;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import net.orient.demo.graph.dao.IdentityGraph;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    @Test
    public void testCreateGraph(){
        IdentityGraph graph = new IdentityGraph("remote:localhost/IdentityUnitTest", "admin", "admin");
        Assert.assertTrue(graph != null);
    }
    @Test
    public void testSingleInsert(){
        IdentityGraph graph = new IdentityGraph("remote:localhost/IdentityUnitTest", "admin", "admin");
        graph.clearGraph();
        graph.addSingleLocal("testID");
        Assert.assertEquals("PERSON_UUID::testID", graph.getUUID("testID").getPersonId());
        Assert.assertEquals("UNKNOWN", graph.getUUID("junk").getPersonId());
        graph.clearGraph();
    }
    @Test
    public void testFullLoadGraph() throws IOException {
        InputStream linkStream = getClass().getClassLoader().getResourceAsStream("test.csv");
        IdentityGraph graph = new IdentityGraph("remote:localhost/IdentityUnitTest", "admin", "admin");
        graph.clearGraph();
        loadGraph(graph, linkStream);
        String abcExpected = "PERSON_UUID::abc";
        String jklExpected = "PERSON_UUID::jkl";
        String stuExpected = "PERSON_UUID::stu";
        Assert.assertEquals(abcExpected, graph.getUUID("abc").getPersonId());
        Assert.assertEquals(jklExpected, graph.getUUID("jkl").getPersonId());
        Assert.assertEquals(jklExpected, graph.getUUID("mno").getPersonId());
        Assert.assertEquals(stuExpected, graph.getUUID("stu").getPersonId());
        Assert.assertEquals(stuExpected, graph.getUUID("345").getPersonId());
        graph.clearGraph();
    }
    private void loadGraph(IdentityGraph graph, InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
}
