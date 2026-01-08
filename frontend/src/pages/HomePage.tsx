import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";

export default function HomePage() {

    return (
        <div className={ "space-y-8 flex flex-col mx-auto" }>
            <p>Home page</p>
            <Button asChild={ true }>
                <NavLink to="/organization/create">Create Orga</NavLink>
            </Button>
            <Button asChild={ true }>
                <NavLink to="/organization/neue-orga-2">See Orga</NavLink>
            </Button>
            <Button asChild={ true }>
                <NavLink to="/event/create">Create Event</NavLink>
            </Button>
        </div>
    )
}
