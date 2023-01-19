package com.github.dcysteine.nesql.server.plugin.base;

import com.github.dcysteine.nesql.server.plugin.base.display.fluid.DisplayFluid;
import com.github.dcysteine.nesql.server.plugin.base.spec.FluidSpec;
import com.github.dcysteine.nesql.server.plugin.base.display.BaseDisplayFactory;
import com.github.dcysteine.nesql.server.service.SearchService;
import com.github.dcysteine.nesql.sql.base.fluid.Fluid;
import com.github.dcysteine.nesql.sql.base.fluid.FluidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

@Controller
@RequestMapping(path = "/fluid")
public class FluidController {
    @Autowired
    private FluidRepository fluidRepository;

    @Autowired
    private BaseDisplayFactory baseDisplayFactory;

    @Autowired
    private SearchService searchService;

    @GetMapping(path = "/view/{fluid_id}")
    public String view(@PathVariable(name = "fluid_id") String id, Model model) {
        Optional<Fluid> fluidOptional = fluidRepository.findById(id);
        if (fluidOptional.isEmpty()) {
            return "not_found";
        }
        Fluid fluid = fluidOptional.get();
        DisplayFluid displayFluid = baseDisplayFactory.buildDisplayFluid(fluid);

        model.addAttribute("fluid", fluid);
        model.addAttribute("displayFluid", displayFluid);
        return "plugin/base/fluid/view";
    }

    @GetMapping(path = "/search")
    public String search(
            @RequestParam(required = false) Optional<String> localizedName,
            @RequestParam(required = false) Optional<String> internalName,
            @RequestParam(required = false) Optional<Integer> fluidId,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        @Nullable
        Specification<Fluid> localizedNameSpec =
                localizedName
                        .filter(Predicate.not(String::isEmpty))
                        .map(FluidSpec::buildLocalizedNameSpec).orElse(null);

        @Nullable
        Specification<Fluid> internalNameSpec =
                internalName
                        .filter(Predicate.not(String::isEmpty))
                        .map(FluidSpec::buildInternalNameSpec).orElse(null);

        @Nullable
        Specification<Fluid> fluidIdSpec =
                fluidId.map(FluidSpec::buildFluidIdSpec).orElse(null);

        Specification<Fluid> spec =
                Specification.allOf(localizedNameSpec, internalNameSpec, fluidIdSpec);
        searchService.handleSearch(
                page, model, fluidRepository,
                spec, FluidSpec.DEFAULT_SORT, baseDisplayFactory::buildDisplayFluidIcon);
        return "plugin/base/fluid/search";
    }
}
