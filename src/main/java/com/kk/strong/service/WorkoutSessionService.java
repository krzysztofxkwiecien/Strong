package com.kk.strong.service;

import com.kk.strong.exception.BodyReportNotFoundException;
import com.kk.strong.exception.ExerciseNotFoundException;
import com.kk.strong.exception.WorkoutSessionNotFoundException;
import com.kk.strong.model.BodyReport;
import com.kk.strong.model.Exercise;
import com.kk.strong.model.WorkoutSession;
import com.kk.strong.model.dto.ExerciseDto;
import com.kk.strong.model.dto.WorkoutSessionDto;
import com.kk.strong.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkoutSessionService {

    private final ExerciseService exerciseService;
    private final WorkoutSessionRepository workoutSessionRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<WorkoutSessionDto> getWorkoutSessions() {
        log.info("Getting all workout sessions");
        return workoutSessionRepository
                .findAll()
                .stream().map(workoutSession -> modelMapper.map(workoutSession, WorkoutSessionDto.class))
                .collect(Collectors.toList());
    }

    public WorkoutSessionDto getWorkoutSession(Long workoutSessionId) {
        log.info("Getting workout session with id: {}", workoutSessionId);
        WorkoutSession workoutSession = workoutSessionRepository
                .findById(workoutSessionId)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(workoutSessionId));
        return modelMapper.map(workoutSession, WorkoutSessionDto.class);
    }

    public void updateWorkoutSession(Long workoutSessionId, WorkoutSessionDto workoutSessionDto) {
        log.info("Updating workout session with id: {}", workoutSessionId);
        WorkoutSession workoutSession = workoutSessionRepository
                .findById(workoutSessionId)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(workoutSessionId));
        modelMapper.map(workoutSessionDto, workoutSession);
    }

    public ExerciseDto addExerciseForWorkoutSession(Long workoutSessionId, ExerciseDto exerciseDto) {
        log.info("Adding exercise for workout session with id: {}", workoutSessionId);
        Exercise exercise = modelMapper.map(exerciseDto, Exercise.class);
        workoutSessionRepository
                .findById(workoutSessionId)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(workoutSessionId))
                .getExercises()
                .add(exercise);
        return exerciseDto;
    }

    public void deleteExerciseForWorkoutSession(Long workoutSessionId, Long exerciseId) {
        log.info("Deleting exercise with id: {}", exerciseId);
        WorkoutSession workoutSession = workoutSessionRepository
                .findById(workoutSessionId)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(workoutSessionId));
        Exercise exerciseToDelete = workoutSession
                .getExercises()
                .stream()
                .filter(exercise -> exercise.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        workoutSession.getExercises().remove(exerciseToDelete);
        exerciseService.deleteExercise(exerciseId);
    }

    public void deleteWorkoutSession(Long workoutSessionId) {
        workoutSessionRepository
                .findById(workoutSessionId)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(workoutSessionId))
                .getExercises()
                .forEach(exercise -> deleteExerciseForWorkoutSession(workoutSessionId, exercise.getId()));
        workoutSessionRepository.deleteById(workoutSessionId);
    }
}
