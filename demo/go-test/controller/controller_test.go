package controller_test

import (
	"context"
	"database/sql"
	"log"
	"os"
	"testing"

	"github.com/JY8752/go-unittest-architecture/controller"
	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/JY8752/go-unittest-architecture/infrastructure/repository"
	mock_api "github.com/JY8752/go-unittest-architecture/mocks/api"
	mock_domain "github.com/JY8752/go-unittest-architecture/mocks/domain"
	"github.com/JY8752/go-unittest-architecture/test"
	"go.uber.org/mock/gomock"

	"github.com/stretchr/testify/assert"
)

const migrationsPath = "../migrations"

var db *sql.DB

func TestMain(m *testing.M) {
	container, err := test.RunMySQLContainer()
	if err != nil {
		container.Close()
		log.Fatal(err)
	}

	db = container.DB

	if err := test.Migrate(db, migrationsPath); err != nil {
		container.Close()
		log.Fatal(err)
	}

	code := m.Run()

	container.Close()
	os.Exit(code)
}

// seed 1000 total weight 71 -> random 67 なのでitem9が抽選されるはず
func TestDraw(t *testing.T) {
	// Arange
	gachaRep := repository.NewGacha(db)
	itemRep := repository.NewItem(db)

	ctrl := gomock.NewController(t)
	t.Cleanup(func() {
		ctrl.Finish()
	})

	sg := mock_domain.NewMockSeedGenerator(ctrl)
	sg.EXPECT().New().Return(int64(1000))

	p := mock_api.NewMockPayment(ctrl)
	p.EXPECT().Buy(100).Return(nil).Times(1)

	sut := controller.NewGacha(gachaRep, itemRep, sg, p)

	expected := domain.NewItem(
		9,
		"item9",
		domain.R,
	)

	// Act
	act, err := sut.Draw(context.Background(), 1)
	if err != nil {
		t.Fatal(err)
	}

	// Assertion
	assert.Equal(t, expected, act)
}
