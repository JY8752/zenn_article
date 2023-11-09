package demo_test

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
)

func Divide(x, y int) (int, error) {
	if y == 0 {
		return 0, errors.New("zero divide")
	}
	return x / y, nil
}

func Test(t *testing.T) {
	tests := map[string]struct {
		X, Y    int
		Want    int
		WantErr error
	}{
		"10 / 5 = 2": {
			X:    10,
			Y:    5,
			Want: 2,
		},
		"10 / 3 = 3": {
			X:    10,
			Y:    3,
			Want: 3,
		},
		"10 / 6 = 1": {
			X:    10,
			Y:    6,
			Want: 1,
		},
		"0 divide is error": {
			Y:       0,
			WantErr: errors.New("zero divide"),
		},
	}

	for name, tt := range tests {
		name, tt := name, tt
		t.Run(name, func(t *testing.T) {
			result, err := Divide(tt.X, tt.Y)

			if tt.WantErr != nil {
				assert.Equal(t, tt.WantErr, err)
			}
			if tt.WantErr == nil && err != nil {
				t.Fatal(err)
			}
			assert.Equal(t, tt.Want, result)
		})
	}
}
