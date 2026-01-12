import ErrorPage from "@/pages/ErrorPage";
import { createBrowserRouter } from "react-router";
import RootLayout from "@/layouts/RootLayout.tsx";
import PageNotFound from "@/pages/PageNotFound.tsx";
import SettingsPage from "@/pages/SettingsPage.tsx";
import ProfilePage from "@/pages/ProfilePage.tsx";
import CreateOrgaPage from "@/pages/CreateOrgaPage.tsx";
import OrganizationDetailsPage from "@/pages/OrganizationDetailsPage.tsx";
import ProtectedRoute from "@/routes/ProtectedRoute.tsx";
import CreateEventPage from "@/pages/CreateEventPage.tsx";
import EventDetailsPage from "@/pages/EventDetailsPage.tsx";
import EventDashBoardPage from "@/pages/EventDashBoardPage.tsx";
import EventsPage from "@/pages/EventsPage.tsx";
import EditEventPage from "@/pages/EditEventPage.tsx";
import OrganizationsPage from "@/pages/OrganizationsPage.tsx";
import DashboardPage from "@/pages/DashboardPage.tsx";

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
                    { path: "/event/edit/:eventId", element: <EditEventPage/> },
                    { path: "/dashboard/:userId", element: <DashboardPage/> },
                    { path: "/dashboard/event/:eventId", element: <EventDashBoardPage/> }
                ]
            },
            { path: "profile/:userId", element: <ProfilePage/> },
            { path: "/events", element: <EventsPage/> },
            { path: "/organizations", element: <OrganizationsPage/> },
            { path: "organization/:orgaSlug", element: <OrganizationDetailsPage/> },
            { path: "/event/:eventId", element: <EventDetailsPage/> },
            { path: "*", element: <PageNotFound/> }
        ],
    }
] );
