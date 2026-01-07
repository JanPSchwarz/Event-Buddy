import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog.tsx";
import { Button } from "@/components/ui/button.tsx";
import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { User } from "lucide-react";
import AddOrganizationOwner from "@/components/organization/AddOrganizationOwner.tsx";
import Text from "@/components/typography/Text.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import {
    getGetOrganizationBySlugQueryKey,
    useRemoveOwnerFromOrganization
} from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

type UpdateOwnerDialogProps = {
    disabled?: boolean,
    orgaData: OrganizationResponseDto,
}

export default function UpdateOwnerDialog( { disabled, orgaData }: Readonly<UpdateOwnerDialogProps> ) {

    const queryClient = useQueryClient();

    const deleteOwner = useRemoveOwnerFromOrganization(
        {
            axios: {
                withCredentials: true,
            }
        }
    )

    const handleDeleteOwner = ( ownerId: string ) => {
        if ( ownerId.length > 0 ) {
            deleteOwner.mutate( {
                    organizationId: orgaData.id || "",
                    userId: ownerId,
                },
                {
                    onSuccess: () => {
                        toast.success( "Owner removed successfully." );
                        queryClient.invalidateQueries( { queryKey: getGetOrganizationBySlugQueryKey( orgaData.slug ) } );
                    },
                    onError: ( error ) => {
                        console.error( "Error removing owner:", error );
                        toast.error( error.response?.data.error || error.message || "Failed to remove user." );
                    }
                }
            )
        }
    }

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant={ "outline" } disabled={ disabled }>
                    Change Owner
                </Button>
            </DialogTrigger>
            <DialogContent className={ "max-h-[90vh]" }>
                <DialogHeader>
                    <DialogTitle>
                        Change Organization Owner
                    </DialogTitle>
                    <DialogDescription>
                        Here you can change the owner of the organization.
                    </DialogDescription>
                </DialogHeader>
                <Separator/>
                <Text styleVariant={ "muted" }>
                    Current Owners:
                </Text>
                {
                    orgaData?.owners?.map( ( owner ) => {
                        return (
                            <div className={ "flex gap-4 items-center justify-between" } key={ owner.name }>
                                <div className={ "flex items-center gap-4" }>
                                    <Avatar>
                                        <AvatarImage src={ owner.avatarUrl }/>
                                        <AvatarFallback>
                                            <User/>
                                        </AvatarFallback>
                                    </Avatar>
                                    <p>{ owner.name }</p>
                                </div>
                                <Button onClick={ () => handleDeleteOwner( owner?.id || "" ) } variant={ "destructive" }
                                        size={ "sm" }>
                                    Delete Owner
                                </Button>
                            </div>
                        )
                    } )
                }
                <AddOrganizationOwner orgData={ orgaData }/>
            </DialogContent>

        </Dialog>
    )
}
