package com.lindar.dotmailer.api;

import com.google.gson.reflect.TypeToken;
import com.lindar.dotmailer.util.DefaultEndpoints;
import com.lindar.dotmailer.vo.api.NewProgramEnrolment;
import com.lindar.dotmailer.vo.api.Program;
import com.lindar.dotmailer.vo.api.ProgramEnrolment;
import com.lindar.dotmailer.vo.internal.DMAccessCredentials;
import com.lindar.wellrested.vo.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProgramResource extends AbstractResource {

    public ProgramResource(DMAccessCredentials accessCredentials) {
        super(accessCredentials);
    }

    public Result<List<Program>> list() {
        return sendAndGetFullList(DefaultEndpoints.PROGRAMS.getPath(), new TypeToken<List<Program>>() {
        });
    }

    public Result<Program> get(Long id) {
        String path = pathWithId(DefaultEndpoints.PROGRAM.getPath(), id);
        return sendAndGet(path, Program.class);
    }

    public Result<ProgramEnrolment> create(NewProgramEnrolment newProgramEnrolment) {
        String path = DefaultEndpoints.PROGRAM_ENROLMENTS.getPath();
        return postAndGet(path, newProgramEnrolment, ProgramEnrolment.class);
    }

    public Result<ProgramEnrolment> getEnrolment(String id) {
        String path = pathWithParam(DefaultEndpoints.PROGRAM_ENROLMENT_BY_ID.getPath(), id);
        return sendAndGet(path, ProgramEnrolment.class);
    }

    public Result<List<ProgramEnrolment>> getEnrolmentsByStatus(String status) {
        String path = pathWithParam(DefaultEndpoints.PROGRAM_ENROLMENTS_BY_STATUS.getPath(), status);
        return sendAndGetFullList(path, new TypeToken<List<ProgramEnrolment>>() {
        });
    }

}
