import {afterEach, describe, expect, test, vi} from 'vitest';
import {getActiveProfile} from './TcCodeGeneration';

describe('getActiveProfile', () => {
    afterEach(() => vi.unstubAllGlobals());

    test('returns the profile from the backend', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('DEV 5.1', {
            headers: {'Content-Type': 'text/plain;charset=UTF-8'}
        })));
        expect(await getActiveProfile()).toBe('DEV 5.1');
    });

    test('does not display the SPA fallback as a profile', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('<!DOCTYPE html><html></html>', {
            headers: {'Content-Type': 'text/html'}
        })));
        expect(await getActiveProfile()).toBe('noenv');
    });

    test('falls back when the backend returns an error', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('', {status: 502})));
        expect(await getActiveProfile()).toBe('noenv');
    });
});