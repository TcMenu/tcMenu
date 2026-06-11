// Platforms.test.ts

import {EmbeddedPlatform} from './Platforms';

describe('EmbeddedPlatform', () => {
    it('should create an instance with the provided properties', () => {
        const friendlyName = 'Test Platform';
        const boardId = '1234';
        const usesProgmem = true;

        const platform = new EmbeddedPlatform(friendlyName, boardId, usesProgmem);

        expect(platform.friendlyName).toBe(friendlyName);
        expect(platform.boardId).toBe(boardId);
        expect(platform.usesProgmem).toBe(usesProgmem);
    });

    it('should handle false "usesProgmem" value correctly', () => {
        const friendlyName = 'Another Platform';
        const boardId = '5678';
        const usesProgmem = false;

        const platform = new EmbeddedPlatform(friendlyName, boardId, usesProgmem);

        expect(platform.friendlyName).toBe(friendlyName);
        expect(platform.boardId).toBe(boardId);
        expect(platform.usesProgmem).toBe(usesProgmem);
    });
});