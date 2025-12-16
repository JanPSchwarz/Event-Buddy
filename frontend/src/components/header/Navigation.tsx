import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
    navigationMenuTriggerStyle
} from "@/components/ui/navigation-menu.tsx";
import { NavLink, useLocation } from "react-router";
import { type NavigationItem, navigations } from "@/lib/navigations.tsx";
import { cn } from "@/lib/utils.ts";

export default function Navigation() {

    const { pathname } = useLocation();

    return (
        <NavigationMenu className={ "hidden md:block" }>
            <NavigationMenuList className={ "gap-4" }>
                { navigations.map( ( { title, href }: NavigationItem ) => (
                        <NavigationMenuItem key={ title }
                        >
                            <NavigationMenuLink asChild
                                                data-active={ pathname === href }
                                                className={ cn( navigationMenuTriggerStyle(), "data-[active=true]:underline data-[active=true]:underline-offset-4 data-[active=true]:decoration-primary  bg-accent hover:underline hover:underline-offset-4 hover:bg-none hover:decoration-primary  max-h-min py-1" ) }>
                                <NavLink to={ href }>{ title }</NavLink>
                            </NavigationMenuLink>
                        </NavigationMenuItem>
                    )
                ) }
            </NavigationMenuList>
        </NavigationMenu>
    )
}

