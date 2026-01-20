import PageWrapper from "@/components/shared/PageWrapper.tsx";
import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { PlayIcon } from "lucide-react";
import { NavLink } from "react-router";

export default function HomePage() {

    return (
        <PageWrapper>
            <Text styleVariant={ "h1" } asTag={ "h1" } className={ "border-b border-primary" }>
                Welcome to Event Buddy!
            </Text>
            <Text styleVariant={ "h4" }>
                Find or create events with ease. Book now.
            </Text>
            <div className={ "w-full flex items-center justify-center relative max-w-[800px]" }>
                <img src={ "/hero.svg" } alt={ "Hero" } className={ "rounded-md m-auto " }/>
                <Button className={ "absolute top-4 right-4" } asChild>
                    <NavLink to={ "/events" }>
                        Get Started
                        <PlayIcon/>
                    </NavLink>
                </Button>
            </div>
        </PageWrapper>
    )
}
