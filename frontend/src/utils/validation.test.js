import { describe, expect, it } from 'vitest';
import { ageOn, maxBirthDate, validateRegister } from './validation';

const today = new Date(2026, 8, 27); // 27/09/2026

const valid = {
  firstName: 'Ana',
  lastName: 'Pérez',
  shippingAddress: 'Zona 10',
  email: 'ana@correo.com',
  birthDate: '1995-05-10',
  password: 'Secreta123',
  confirmPassword: 'Secreta123',
};

describe('validateRegister', () => {
  it('acepta un formulario válido', () => {
    expect(validateRegister(valid, today)).toEqual({});
  });

  it('exige todos los campos', () => {
    const errors = validateRegister({}, today);
    expect(Object.keys(errors)).toEqual(
      expect.arrayContaining(['firstName', 'lastName', 'shippingAddress', 'birthDate', 'email', 'password']),
    );
  });

  it.each(['sin-arroba.com', 'a@b', 'a@b.c', 'con espacio@x.com'])('rechaza el email %s', (email) => {
    expect(validateRegister({ ...valid, email }, today).email).toBe('Formato de email inválido');
  });

  it('rechaza menores de edad y acepta a quien cumple 18 hoy', () => {
    expect(validateRegister({ ...valid, birthDate: '2008-09-28' }, today).birthDate).toBeDefined();
    expect(validateRegister({ ...valid, birthDate: '2008-09-27' }, today).birthDate).toBeUndefined();
  });

  it('valida que las contraseñas coincidan', () => {
    expect(validateRegister({ ...valid, confirmPassword: 'Otra1234' }, today).confirmPassword).toBeDefined();
  });
});

describe('fechas', () => {
  it('calcula la edad cumplida', () => {
    expect(ageOn('2000-09-27', today)).toBe(26);
    expect(ageOn('2000-09-28', today)).toBe(25);
  });

  it('maxBirthDate es hoy menos 18 años', () => {
    expect(maxBirthDate(today)).toBe('2008-09-27');
  });
});
