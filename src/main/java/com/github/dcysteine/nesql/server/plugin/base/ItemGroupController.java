package com.github.dcysteine.nesql.server.plugin.base;

import com.github.dcysteine.nesql.server.plugin.base.display.item.DisplayItemGroup;
import com.github.dcysteine.nesql.server.plugin.base.display.BaseDisplayService;
import com.github.dcysteine.nesql.server.service.SearchService;
import com.github.dcysteine.nesql.sql.base.item.ItemGroup;
import com.github.dcysteine.nesql.sql.base.item.ItemGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping(path = "/itemgroup")
public class ItemGroupController {
    @Autowired
    private ItemGroupRepository itemGroupRepository;

    @Autowired
    private BaseDisplayService baseDisplayService;

    @Autowired
    private SearchService searchService;

    @GetMapping(path = "/{item_group_id}")
    public String view(@PathVariable(name = "item_group_id") String id, Model model) {
        Optional<ItemGroup> itemGroupOptional = itemGroupRepository.findById(id);
        if (itemGroupOptional.isEmpty()) {
            return "not_found";
        }
        ItemGroup itemGroup = itemGroupOptional.get();
        DisplayItemGroup displayItemGroup = baseDisplayService.buildDisplayItemGroup(itemGroup);

        model.addAttribute("itemGroup", itemGroup);
        model.addAttribute("displayItemGroup", displayItemGroup);
        return "plugin/base/itemgroup/item_group";
    }

    @GetMapping(path = "/search")
    public String search() {
        // TODO add a search page
        return "redirect:all";
    }

    @GetMapping(path = "/all")
    public String all(@RequestParam(defaultValue = "1") int page, Model model) {
        return searchService.handleGetAll(
                page, model, itemGroupRepository, baseDisplayService::buildDisplayItemGroupIcon);
    }
}