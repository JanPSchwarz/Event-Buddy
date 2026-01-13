import Text from "@/components/typography/Text.tsx";
import type { ElementType } from "react";
import { twMerge } from "tailwind-merge";

type MainHeadingProps = {
    heading: string,
    subheading: string,
    Icon?: ElementType,
    iconClassNames?: string,
}

export default function MainHeading( { heading, subheading, Icon, iconClassNames }: Readonly<MainHeadingProps> ) {

    return (
        <div className={ "w-full flex justify-between border-b border-primary" }>
            <Text asTag={ "h1" } styleVariant={ "h1" } className={ "relative" }>
                { heading }
                { Icon &&
                    <Icon className={ twMerge( "inline -translate-y-1/2", iconClassNames ) }/> }
            </Text>
            <Text asTag={ "span" } styleVariant={ "smallMuted" } className={ "mt-auto" }>
                { subheading }
            </Text>
        </div>
    )
}
