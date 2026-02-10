import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList
} from "@/components/ui/navigation-menu.tsx";
import { NavLink, useLocation } from "react-router";
import { type NavigationItem, navigations } from "@/lib/navigations.tsx";

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
                                                className={ "max-h-min py-1" }>
                                <NavLink to={ href }>{ title }</NavLink>
                            </NavigationMenuLink>
                        </NavigationMenuItem>
                    )
                ) }
            </NavigationMenuList>
        </NavigationMenu>
    )
}

