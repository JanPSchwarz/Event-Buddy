import { Outlet, useLocation } from "react-router";
import HomePage from "@/pages/HomePage.tsx";
import Header from "@/components/Header.tsx";

export default function RootLayout() {
    const location = useLocation();
    const isHomePage = location.pathname === '/';

    return (

        <>
            <Header/>
            <div className={ "mt-8 flex-1 flex items-stretch justify-stretch w-full" }>
                { isHomePage && <HomePage/> }
                <Outlet/>
            </div>
        </>
    )
}