package net.orient.demo.web;

import com.google.common.base.Optional;
import com.tinkerpop.blueprints.Vertex;
import net.orient.demo.graph.dao.IdentityGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Locale;


@Controller
public class MainController {
    private static final String INVALID_ID = "INVALID ID";
    @Autowired
    private IdentityGraph graph;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Locale locale, Model model){
        return "home";
    }

    @RequestMapping(value = "/lookup", method = RequestMethod.POST, consumes = "text/plain")
    public String lookupID(@RequestBody String payload, Model model){
        Optional<String> id = parseID(payload);
        if(id.isPresent()){
            Identity identity = graph.getUUID(id.get());
            model.addAttribute("localId", identity.getLocalId());
            model.addAttribute("universalId", identity.getPersonId());
        } else {
            model.addAttribute("localId", INVALID_ID);
            model.addAttribute("universalId", INVALID_ID);
        }
        System.out.println(payload);
        return "lookup";
    }

    private Optional<String> parseID(String payload) {
        try {
            String[] tokens = payload.split("=");
            return Optional.fromNullable(tokens[1]);
        } catch (Exception e){
            return Optional.absent();
        }
    }
}
