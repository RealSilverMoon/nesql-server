package com.github.dcysteine.nesql.server;

import com.github.dcysteine.nesql.server.config.ExternalConfig;
import com.github.dcysteine.nesql.server.util.NumberUtil;
import com.github.dcysteine.nesql.sql.base.fluid.FluidRepository;
import com.github.dcysteine.nesql.sql.base.item.ItemRepository;
import com.github.dcysteine.nesql.sql.base.recipe.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves root-level endpoints. */
@Controller
public class RootController {
    @Autowired
    ExternalConfig externalConfig;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private FluidRepository fluidRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        model.addAttribute(
                "itemCount", NumberUtil.formatInteger(itemRepository.count()));
        model.addAttribute(
                "fluidCount", NumberUtil.formatInteger(fluidRepository.count()));
        model.addAttribute(
                "recipeCount", NumberUtil.formatInteger(recipeRepository.count()));
        return "index";
    }

    @GetMapping("/notfound")
    public String notFound() {
        return "not_found";
    }

    @GetMapping("/shutdown")
    public String shutDown() {
        if (!externalConfig.isShutdownEnabled()) {
            return "shutdown_disabled";
        }

        // We need to actually shut down in a separate thread, so that we can proceed with serving
        // the shutdown page.
        new Thread(() -> SpringApplication.exit(context)).start();
        return "shutdown";
    }
}