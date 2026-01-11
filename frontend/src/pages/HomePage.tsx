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
                <NavLink to="/organization/test-orga">See Orga</NavLink>
            </Button>
            <Button asChild={ true }>
                <NavLink to="/event/create">Create Event</NavLink>
            </Button>
            <Button asChild={ true }>
                <NavLink to="/event/69610dce5a407520acc50037">Event details</NavLink>
            </Button>
            <Button asChild={ true }>
                <NavLink to="/events">Events Page</NavLink>
            </Button>
            <Button asChild={ true }>
                <NavLink to="/event/dashboard/69610dce5a407520acc50037">Event Dashboard</NavLink>
            </Button>

            <Button asChild={ true }>
                <NavLink to="/event/edit/69610dce5a407520acc50037">Edit Event</NavLink>
            </Button>
        </div>
    )
}
