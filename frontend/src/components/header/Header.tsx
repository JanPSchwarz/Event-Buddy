import Navigation from "@/components/header/Navigation.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { Ticket } from 'lucide-react';
import UserHandler from "@/components/header/UserHandler.tsx";
import { ThemeToggle } from "@/components/header/ThemeToggle.tsx";
import Text from "@/components/typography/Text.tsx";
import MobileNavigation from "@/components/header/MobileNavigation.tsx";


export default function Header() {

    return (
        <header
            className={ "bg-muted w-full fixed top-0 z-50 flex max-w-[1400px] justify-between items-center gap-4 p-2 px-3" }>
            <Navigation/>
            <MobileNavigation/>
            <div className={ "flex gap-4 items-center" }>
                <UserHandler/>
                <ThemeToggle/>
                <Separator orientation={ "vertical" } className={ "data-[orientation=vertical]:h-8" }/>
                <div className={ "flex gap-2 items-center" }>
                    <Text className={ "font-brand md:text-3xl text-primary/80 tracking-wide" } asTag={ "p" }
                          styleVariant={ "h2" }>
                        Event Buddy
                    </Text>
                    <div
                        className={ "ring rounded-full flex justify-center items-center size-8 md:size-10 p-1 ring-primary" }>
                        <Ticket className={ "stroke-primary rotate-45 size-5 md:size-6" }/>
                    </div>
                </div>
            </div>
            <Separator className={ "bg-primary w-full absolute bottom-0 left-0" }/>
        </header>
    )
}
