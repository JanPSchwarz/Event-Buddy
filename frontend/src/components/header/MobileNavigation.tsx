import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Menu } from 'lucide-react';
import { navigations } from "@/lib/navigations.tsx";
import { NavLink, useLocation } from "react-router";


export default function MobileNavigation() {

    const { pathname } = useLocation();

    return (
        <div className={ "md:hidden" }>
            <Sheet>
                <SheetTrigger asChild>
                    <Button variant={ "outline" }>
                        <Menu/>
                        <p className={ "sr-only" }>Toggle Navigation Menu</p>
                    </Button>
                </SheetTrigger>
                <SheetContent side={ "left" } className={ "max-w-min sm:max-w-min px-4 text-nowrap" }>
                    <SheetHeader>
                        <SheetTitle className={ "px-2" }>
                            Navigation
                        </SheetTitle>
                    </SheetHeader>
                    <div className={ "flex flex-col gap-4" }>
                        { navigations.map( ( { href, title } ) => (
                            <Button
                                key={ title }
                                variant={ "ghost" }
                                asChild
                            >
                                <NavLink to={ href }
                                         className={ `${ pathname === href && "underline decoration-primary underline-offset-2" }` }>
                                    { title }
                                </NavLink>
                            </Button>
                        ) )
                        }
                    </div>
                </SheetContent>
            </Sheet>
        </div>
    )
}
