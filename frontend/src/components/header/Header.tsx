import Navigation from "@/components/header/Navigation.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { Ticket } from 'lucide-react';
import UserHandler from "@/components/header/UserHandler.tsx";
import { ThemeToggle } from "@/components/header/ThemeToggle.tsx";


export default function Header() {

    return (
        <>
            <header className={ "bg-accent flex justify-between items-center p-2 px-3" }>
                <Navigation/>
                <div className={ "flex gap-4 items-center" }>
                    <UserHandler/>
                    <ThemeToggle/>
                    <div
                        className={ "ring ml-8 rounded-full flex justify-center items-center w-10 h-10 p-1 ring-primary bg-accent" }>
                        <Ticket className={ "stroke-primary rotate-45" }/>
                    </div>
                </div>
            </header>
            <Separator className={ "bg-primary" }/>
        </>
    )
}
