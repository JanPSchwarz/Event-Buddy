package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eventbuddy.backend.services.FakeDataService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fake-data")
@Tag(name = "Fake data", description = "creates and deletes fake data for development purposes")
@Profile("dev")
public class FakeDataController {

    FakeDataService fakeDataService;

    public FakeDataController( FakeDataService fakeDataService ) {
        this.fakeDataService = fakeDataService;
    }

    @PostMapping("/create-fake-users")
    @Operation(
            summary = "Create fake users",
            description = "Creates custom number fake users for testing purposes."
    )
    public void createFakeUsers() {
        fakeDataService.createFakeUsers( 10 );
    }

    @DeleteMapping("/delete-all-users")
    @Operation(
            summary = "Delete all fake users",
            description = "Deletes all users from the system."
    )
    public void deleteAllFakeUsers() {
        fakeDataService.deleteAllFakeUsers();
    }
}
