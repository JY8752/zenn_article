package domain_test

import (
	"testing"

	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/stretchr/testify/assert"
)

func TestDraw(t *testing.T) {
	t.Parallel()
	tests := map[string]struct {
		Weights []int
		Expect  int64
	}{
		"case1: when seed 10 total weights 16, rand 14": {
			Weights: []int{1, 5, 10},
			Expect:  3,
		},
		"case2: when seed 10 total weights 16, rand 14": {
			Weights: []int{1, 10, 5},
			Expect:  3,
		},
		"case3: when seed 10 total weights 16, rand 14": {
			Weights: []int{5, 1, 10},
			Expect:  3,
		},
		"case4: when seed 10 total weights 16, rand 14": {
			Weights: []int{5, 10, 1},
			Expect:  2,
		},
		"case5: when seed 10 total weights 16, rand 14": {
			Weights: []int{10, 1, 5},
			Expect:  3,
		},
		"case6: when seed 10 total weights 16, rand 14": {
			Weights: []int{10, 5, 1},
			Expect:  2,
		},
		"case1: only one item": {
			Weights: []int{1},
			Expect:  1,
		},
		"case2: only one item": {
			Weights: []int{1000},
			Expect:  1,
		},
		"weights 0": {
			Weights: []int{0},
			Expect:  1,
		},
	}

	for name, tt := range tests {
		name, tt := name, tt
		t.Run(name, func(t *testing.T) {
			// Arange
			t.Parallel()
			sut := domain.NewGacha(newGachaItemWeights(t, tt.Weights))
			seed := int64(10)

			// Act
			// itemId, err := sut.Draw(seed)
			// if err != nil {
			// 	t.Fatal(err)
			// }
			itemId := execute(t, func() (int64, error) {
				return sut.Draw(seed)
			})

			// Asertion
			assert.Equal(t, tt.Expect, itemId)
		})
	}
}

func newGachaItemWeights(t *testing.T, weights []int) domain.GachaItemWeights {
	t.Helper()
	gachaItemWeights := make(domain.GachaItemWeights, len(weights))
	for i, w := range weights {
		gachaItemWeights[i] = struct {
			ItemId int64
			Weight int
		}{
			ItemId: int64(i + 1),
			Weight: w,
		}
	}
	return gachaItemWeights
}

func execute[T any](t *testing.T, f func() (T, error)) T {
	t.Helper()
	v, err := f()
	if err != nil {
		t.Fatal(err)
	}
	return v
}
