import {describe, it, expect, beforeEach, vi} from 'vitest';
import {getTasks, createTask, updateTaskStatus} from './tasks';
import type {TaskStatus} from '../types';

/**
 * Unit tests for tasks service.
 * Tests cover task CRUD operations and status management.
 */
describe('Tasks Service', () => {
    // Mock crypto.randomUUID for consistent test results
    beforeEach(() => {
        vi.stubGlobal('crypto', {
            randomUUID: vi.fn(() => 'test-uuid-123'),
        });
    });

    // We need to reset the module state between test suites
    // Since tasks is an in-memory array, we test in sequence

    describe('getTasks', () => {
        it('should return an empty array initially', async () => {
            // Note: This test depends on module-level state
            // In a real app, you might want to reset state between tests
            const tasks = await getTasks();
            expect(Array.isArray(tasks)).toBe(true);
        });

        it('should return a promise', () => {
            const result = getTasks();
            expect(result).toBeInstanceOf(Promise);
        });
    });

    describe('createTask', () => {
        it('should create a task with provided properties', async () => {
            // Arrange
            const taskData = {
                title: 'Test Task',
                description: 'Test Description',
                status: 'todo' as TaskStatus,
                priority: 'high' as const,
                language: "en"
            };

            // Act
            const task = await createTask(taskData);

            // Assert
            expect(task.title).toBe('Test Task');
            expect(task.description).toBe('Test Description');
            expect(task.status).toBe('todo');
            expect(task.priority).toBe('high');
        });

        it('should generate a unique id for the task', async () => {
            // Arrange
            const taskData = {
                title: 'Task with ID',
                status: 'todo' as TaskStatus,
                language: "en"
            };

            // Act
            const task = await createTask(taskData);

            // Assert
            expect(task.id).toBeDefined();
            expect(task.id).toBe('test-uuid-123');
        });

        it('should set createdAt timestamp', async () => {
            // Arrange
            const beforeCreate = new Date().toISOString();
            const taskData = {
                title: 'Task with timestamp',
                status: 'todo' as TaskStatus,
                language: "en"

            };

            // Act
            const task = await createTask(taskData);
            const afterCreate = new Date().toISOString();

            // Assert
            expect(task.createdAt).toBeDefined();
            expect(task.createdAt >= beforeCreate).toBe(true);
            expect(task.createdAt <= afterCreate).toBe(true);
        });

        it('should add task to the tasks list', async () => {
            // Arrange
            const initialTasks = await getTasks();
            const initialLength = initialTasks.length;

            const taskData = {
                title: 'New Task',
                status: 'todo' as TaskStatus,
                language: "en"

            };

            // Act
            await createTask(taskData);
            const updatedTasks = await getTasks();

            // Assert
            expect(updatedTasks.length).toBe(initialLength + 1);
        });

        it('should return a promise that resolves to the created task', async () => {
            // Arrange
            const taskData = {
                title: 'Promise Test Task',
                status: 'in_progress' as TaskStatus,
                language: "en"
            };

            // Act
            const result = createTask(taskData);

            // Assert
            expect(result).toBeInstanceOf(Promise);
            const task = await result;
            expect(task.title).toBe('Promise Test Task');
        });

        it('should handle optional fields', async () => {
            // Arrange
            const taskData = {
                title: 'Minimal Task',
                status: 'todo' as TaskStatus,
                language: "en"
            };

            // Act
            const task = await createTask(taskData);

            // Assert
            expect(task.title).toBe('Minimal Task');
            expect(task.description).toBeUndefined();
            expect(task.deadline).toBeUndefined();
            expect(task.priority).toBeUndefined();
        });

        it('should handle task with deadline', async () => {
            // Arrange
            const deadline = '2024-12-31T23:59:59';
            const taskData = {
                title: 'Task with Deadline',
                status: 'todo' as TaskStatus,
                deadline,
                language: "en"
            };

            // Act
            const task = await createTask(taskData);

            // Assert
            expect(task.deadline).toBe(deadline);
        });
    });

    describe('updateTaskStatus', () => {
        it('should update task status to completed', async () => {
            // Arrange - Create a task first
            vi.stubGlobal('crypto', {
                randomUUID: vi.fn(() => 'status-update-uuid'),
            });

            const taskData = {
                title: 'Task to Update',
                status: 'todo' as TaskStatus,
                language: "en"
            };
            const createdTask = await createTask(taskData);

            // Act
            const updatedTask = await updateTaskStatus(createdTask.id, 'completed');

            // Assert
            expect(updatedTask).not.toBeNull();
            expect(updatedTask?.status).toBe('completed');
        });

        it('should update task status to in_progress', async () => {
            // Arrange
            vi.stubGlobal('crypto', {
                randomUUID: vi.fn(() => 'in-progress-uuid'),
            });

            const taskData = {
                title: 'Task for Progress Update',
                status: 'todo' as TaskStatus,
                language: "en"

            };
            const createdTask = await createTask(taskData);

            // Act
            const updatedTask = await updateTaskStatus(createdTask.id, 'in_progress');

            // Assert
            expect(updatedTask).not.toBeNull();
            expect(updatedTask?.status).toBe('in_progress');
        });

        it('should return null for non-existent task id', async () => {
            // Act
            const result = await updateTaskStatus('non-existent-id', 'completed');

            // Assert
            expect(result).toBeNull();
        });

        it('should preserve other task properties when updating status', async () => {
            // Arrange
            vi.stubGlobal('crypto', {
                randomUUID: vi.fn(() => 'preserve-props-uuid'),
            });

            const taskData = {
                title: 'Task with Properties',
                description: 'Description',
                status: 'todo' as TaskStatus,
                priority: 'high' as const,
                language: "en"

            };
            const createdTask = await createTask(taskData);

            // Act
            const updatedTask = await updateTaskStatus(createdTask.id, 'completed');

            // Assert
            expect(updatedTask?.title).toBe('Task with Properties');
            expect(updatedTask?.description).toBe('Description');
            expect(updatedTask?.priority).toBe('high');
        });

        it('should return a promise', () => {
            // Act
            const result = updateTaskStatus('some-id', 'completed');

            // Assert
            expect(result).toBeInstanceOf(Promise);
        });
    });
});

