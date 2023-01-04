package com.github.dcysteine.nesql.server.plugin.base.display.item;

import com.github.dcysteine.nesql.server.display.Icon;
import com.github.dcysteine.nesql.server.plugin.base.display.recipe.DisplayRecipe;
import com.github.dcysteine.nesql.server.util.Constants;
import com.github.dcysteine.nesql.server.util.UrlBuilder;
import com.github.dcysteine.nesql.sql.base.item.ItemGroup;
import com.github.dcysteine.nesql.sql.base.item.ItemRepository;
import com.github.dcysteine.nesql.sql.base.item.ItemStack;
import com.github.dcysteine.nesql.sql.base.item.WildcardItemStack;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import java.util.SortedSet;

@AutoValue
public abstract class DisplayItemGroup implements Comparable<DisplayItemGroup> {
    public static DisplayItemGroup create(ItemGroup itemGroup, ItemRepository itemRepository) {
        ImmutableList<Icon> recipesWithInput =
                itemGroup.getRecipesWithInput().stream()
                        .sorted()
                        .map(DisplayRecipe::buildIcon)
                        .collect(ImmutableList.toImmutableList());

        ImmutableList<Icon> itemStacks =
                itemGroup.getItemStacks().stream()
                        .map(DisplayItemStack::buildIcon)
                        .collect(ImmutableList.toImmutableList());
        ImmutableList<Icon> wildcardItemStacks =
                itemGroup.getWildcardItemStacks().stream()
                        .map(wildcardItemStack ->
                                DisplayWildcardItemStack.buildIcon(
                                        wildcardItemStack, itemRepository))
                        .collect(ImmutableList.toImmutableList());

        ImmutableListMultimap.Builder<Integer, Icon> builder =
                ImmutableListMultimap.builder();
        for (WildcardItemStack wildcardItemStack : itemGroup.getWildcardItemStacks()) {
            int itemId = wildcardItemStack.getItemId();
            itemRepository.findByItemId(itemId).stream()
                    .sorted()
                    .map(item -> new ItemStack(item, wildcardItemStack.getStackSize()))
                    .map(DisplayItemStack::buildIcon)
                    .forEach(itemStack -> builder.put(itemId, itemStack));
        }
        ImmutableListMultimap<Integer, Icon> resolvedWildcardItemStacks = builder.build();

        return new AutoValue_DisplayItemGroup(
                itemGroup, buildIcon(itemGroup, itemRepository),
                recipesWithInput, itemStacks, wildcardItemStacks, resolvedWildcardItemStacks);
    }

    public static Icon buildIcon(ItemGroup itemGroup, ItemRepository itemRepository) {
        SortedSet<ItemStack> itemStacks = itemGroup.getItemStacks();
        SortedSet<WildcardItemStack> wildcardItemStacks = itemGroup.getWildcardItemStacks();

        int size = itemStacks.size();
        for (WildcardItemStack wildcardItemStack : wildcardItemStacks) {
            int itemId = wildcardItemStack.getItemId();
            size += itemRepository.findByItemId(itemId).size();
        }

        String url = UrlBuilder.buildItemGroupUrl(itemGroup);
        Icon icon;
        if (!itemStacks.isEmpty()) {
            icon = DisplayItemStack.buildIcon(itemStacks.first()).toBuilder()
                    .setDescription(String.format("Item Group (%d item stacks)", size))
                    .setUrl(url)
                    .build();
        } else if (!wildcardItemStacks.isEmpty()) {
            icon = DisplayWildcardItemStack.buildIcon(wildcardItemStacks.first(), itemRepository)
                    .toBuilder()
                    .setDescription(
                            String.format(
                                    "Wildcard Item Group (%d keys, %d item stacks)",
                                    wildcardItemStacks.size(), size))
                    .setUrl(url)
                    .build();
        } else {
            icon = Icon.builder()
                    .setDescription("Item Group (empty)")
                    .setUrl(url)
                    .setImageFilePath(Constants.MISSING_IMAGE)
                    .build();
        }
        return icon;
    }

    public abstract ItemGroup getItemGroup();
    public abstract Icon getIcon();
    public abstract ImmutableList<Icon> getRecipesWithInput();
    public abstract ImmutableList<Icon> getItemStacks();
    public abstract ImmutableList<Icon> getWildcardItemStacks();
    public abstract ImmutableListMultimap<Integer, Icon> getResolvedWildcardItemStacks();

    @Override
    public int compareTo(DisplayItemGroup other) {
        return getItemGroup().compareTo(other.getItemGroup());
    }
}