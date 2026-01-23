import { RouterProvider } from "react-router";
import { routes } from "@/routes/routes.tsx";
import "./lib/zodGlobalConfig.ts"

function App() {

    return (
        <RouterProvider router={ routes }/>
    )
}

export default App
