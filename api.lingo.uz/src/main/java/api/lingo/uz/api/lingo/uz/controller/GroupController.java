package api.lingo.uz.api.lingo.uz.controller;

import api.lingo.uz.api.lingo.uz.dto.GroupDto;
import api.lingo.uz.api.lingo.uz.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @PostMapping("")
    public ResponseEntity<GroupDto> createNewGroup(@Valid @RequestBody GroupDto groupDto,
                                                   Principal principal) {
        GroupDto response = groupService.createNewGroup(groupDto, principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateGroup(@PathVariable(name = "id") String id,
                                               @Valid @RequestBody GroupDto groupDto,
                                               Principal principal) {
        Boolean response = groupService.updateGroup(id, groupDto, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteGroup(@PathVariable(name = "id") String id,
                                               Principal principal) {
        Boolean response = groupService.deleteGroup(id, principal);
        return ResponseEntity.ok(response);
    }
}
