import ErrorPage from "@/pages/ErrorPage";
import { createBrowserRouter } from "react-router";
import RootLayout from "@/layouts/RootLayout.tsx";
import PageNotFound from "@/pages/PageNotFound.tsx";
import SettingsPage from "@/pages/SettingsPage.tsx";
import ProfilePage from "@/pages/ProfilePage.tsx";
import CreateOrgaPage from "@/pages/CreateOrgaPage.tsx";
import OrganizationPage from "@/pages/OrganizationPage.tsx";
import ProtectedRoute from "@/routes/ProtectedRoute.tsx";
import CreateEventPage from "@/pages/CreateEventPage.tsx";
import EventDetailsPage from "@/pages/EventDetailsPage.tsx";
import EventDashBoardPage from "@/pages/EventDashBoardPage.tsx";
import EventsPage from "@/pages/EventsPage.tsx";
import EditEventPage from "@/pages/EditEventPage.tsx";

export const routes = createBrowserRouter( [
    {
        path: "/",
        element: <RootLayout/>,
        errorElement: <ErrorPage/>,
        children: [
            {
                element: <ProtectedRoute/>,
                children: [
                    { path: "settings/:userId", element: <SettingsPage/> },
                    { path: "organization/create", element: <CreateOrgaPage/> },
                    { path: "event/create", element: <CreateEventPage/> },
                    { path: "/event/dashboard/:eventId", element: <EventDashBoardPage/> },
                    { path: "/event/edit/:eventId", element: <EditEventPage/> }
                ]
            },
            { path: "/events", element: <EventsPage/> },
            { path: "profile/:userId", element: <ProfilePage/> },
            { path: "organization/:orgaSlug", element: <OrganizationPage/> },
            { path: "/event/:eventId", element: <EventDetailsPage/> },
            { path: "*", element: <PageNotFound/> }
        ],
    }
] );
