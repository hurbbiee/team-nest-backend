package com.teamnest.hrm.department;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.teamnest.hrm.department.dto.CreateDepartmentRequest;
import com.teamnest.hrm.department.dto.DeleteDepartmentResponse;
import com.teamnest.hrm.department.dto.DepartmentResponse;
import com.teamnest.hrm.department.dto.UpdateDepartmentRequest;
import com.teamnest.hrm.common.response.PageResponse;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void shouldCreateDepartment() throws Exception {
        Instant now = Instant.parse("2026-10-11T12:00:00Z");
        DepartmentResponse response = new DepartmentResponse(
                1L, "ENGINEERING", "Engineering", true, now, now);
        when(departmentService.create(any(CreateDepartmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "ENGINEERING",
                                  "name": "Engineering"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Department created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("ENGINEERING"))
                .andExpect(jsonPath("$.data.name").value("Engineering"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(departmentService).create(any(CreateDepartmentRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "invalid code",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void shouldReturnConflictWhenDepartmentCodeAlreadyExists() throws Exception {
        when(departmentService.create(any(CreateDepartmentRequest.class)))
                .thenThrow(new DepartmentCodeAlreadyExistsException("ENGINEERING"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "ENGINEERING",
                                  "name": "Engineering"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Department code already exists: ENGINEERING"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void shouldFindDepartmentById() throws Exception {
        Instant now = Instant.parse("2026-10-11T12:00:00Z");
        DepartmentResponse response = new DepartmentResponse(
                1L, "ENGINEERING", "Engineering", true, now, now);
        when(departmentService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Department retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("ENGINEERING"))
                .andExpect(jsonPath("$.data.name").value("Engineering"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(departmentService).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDepartmentDoesNotExist() throws Exception {
        when(departmentService.findById(999L))
                .thenThrow(new DepartmentNotFoundException(999L));

        mockMvc.perform(get("/api/departments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Department not found with id: 999"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldFindDepartmentsWithPagination() throws Exception {
        Instant now = Instant.parse("2026-10-11T12:00:00Z");
        DepartmentResponse department = new DepartmentResponse(
                1L, "ENGINEERING", "Engineering", true, now, now);
        PageResponse<DepartmentResponse> pageResponse = new PageResponse<>(
                List.of(department), 1, 10, 1, 1);
        when(departmentService.findAll(1, 10, "engineer")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/departments")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Departments retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].code").value("ENGINEERING"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(departmentService).findAll(1, 10, "engineer");
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldUpdateDepartment() throws Exception {
        Instant now = Instant.parse("2026-10-12T12:00:00Z");
        DepartmentResponse response = new DepartmentResponse(
                1L, "ENGINEERING", "Software Engineering", false, now, now);
        when(departmentService.update(eq(1L), any(UpdateDepartmentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Software Engineering",
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Department updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("ENGINEERING"))
                .andExpect(jsonPath("$.data.name").value("Software Engineering"))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(departmentService).update(eq(1L), any(UpdateDepartmentRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {
        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "active": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldSoftDeleteDepartment() throws Exception {
        Instant deletedAt = Instant.parse("2026-10-12T12:00:00Z");
        DeleteDepartmentResponse response = new DeleteDepartmentResponse(1L, true, deletedAt);
        when(departmentService.delete(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Department deleted successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.deletedAt").value("2026-10-12T12:00:00Z"));

        verify(departmentService).delete(1L);
    }
}
