export type LoginProvider = {
    provider: string;
    callbackUrl: string;
    logoSrc: string;
}

export const loginProvider: LoginProvider[] = [
    { provider: "Github", callbackUrl: "/oauth2/authorization/github", logoSrc: "/github.svg" }
]