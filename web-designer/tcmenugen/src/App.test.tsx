import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from './App';

test('renders app header', () => {
  render(
    <MemoryRouter>
      <App />
    </MemoryRouter>
  );
  const headerElements = screen.getAllByText(/TcMenu Turbo/i);
  expect(headerElements.length).toBeGreaterThan(0);
});
